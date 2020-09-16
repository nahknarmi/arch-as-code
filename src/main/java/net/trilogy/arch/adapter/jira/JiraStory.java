package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static net.trilogy.arch.adapter.jira.JiraStory.JiraTdd.jiraTddFrom;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class JiraStory {
    private final String title;
    private final List<JiraTdd> tdds;
    private final List<JiraFunctionalRequirement> functionalRequirements;

    public JiraStory(ArchitectureUpdate au,
                     ArchitectureDataStructure beforeAuArchitecture,
                     ArchitectureDataStructure afterAuArchitecture,
                     FeatureStory featureStory) throws InvalidStoryException {
        title = featureStory.getTitle();
        tdds = getTdds(au, beforeAuArchitecture, afterAuArchitecture, featureStory);
        functionalRequirements = getFunctionalRequirements(au, featureStory);
    }

    static String buildTddRow(JiraTdd tdd) {
        if (tdd.hasTddContent()) {
            return "| " + tdd.getId() + " | " + tdd.getText() + " |\n";
        } else {
            return "| " + tdd.getId() + " | {noformat}" + tdd.getText() + "{noformat} |\n";
        }
    }

    static String makeFunctionalRequirementRow(JiraFunctionalRequirement funcReq) {
        return "| " + funcReq.getId() + " | "
                + funcReq.getSource()
                + " | {noformat}" + funcReq.getText() + "{noformat} |\n";
    }

    static String makeDescription(JiraStory story) {
        return makeFunctionalRequirementTable(story) +
                makeTddTablesByComponent(story);
    }

    private static String makeTddTablesByComponent(JiraStory story) {
        final var compMap = story.getTdds().stream()
                .collect(groupingBy(JiraTdd::getComponentPath));

        return "h3. Technical Design:\n" + compMap.entrySet().stream()
                .map(it -> "h4. Component: "
                        + it.getKey()
                        + "\n||TDD||Description||\n"
                        + it.getValue().stream()
                        .map(JiraStory::buildTddRow)
                        .collect(joining()))
                .collect(joining());
    }

    private static String makeFunctionalRequirementTable(JiraStory story) {
        return "h3. Implements functionality:\n" +
                "||Id||Source||Description||\n" +
                story.getFunctionalRequirements().stream()
                        .map(JiraStory::makeFunctionalRequirementRow)
                        .collect(joining());
    }

    public IssueInput toJira(String epicKey, String projectId) {
/*
                .map(story -> new JSONObject(Map.of(
                        "fields", Map.of(
                                "customfield_10002", epicKey,
                                "project", Map.of("id", projectId),
                                "summary", story.getTitle(),
                                "issuetype", Map.of("name", "Feature Story"),
                                "description", makeDescription(story)))))
 */
        return new IssueInputBuilder()
                .setFieldValue("customfield_10002", epicKey)
                .setFieldValue("project", projectId)
                .setFieldValue("summary", title)
                .setFieldValue("issuetype", ComplexIssueInputFieldValue.with("name", "Feature Story"))
                .setFieldValue("description", makeDescription(this))
                .build();
    }

    private static List<JiraFunctionalRequirement> getFunctionalRequirements(ArchitectureUpdate au, FeatureStory featureStory) throws InvalidStoryException {
        final var requirements = new ArrayList<JiraFunctionalRequirement>();
        for (var reqId : featureStory.getRequirementReferences()) {
            if (!au.getFunctionalRequirements().containsKey(reqId))
                throw new InvalidStoryException();
            requirements.add(new JiraFunctionalRequirement(reqId, au.getFunctionalRequirements().get(reqId)));
        }
        return requirements;
    }

    private static List<JiraTdd> getTdds(
            ArchitectureUpdate au,
            ArchitectureDataStructure beforeAuArchitecture, ArchitectureDataStructure afterAuArchitecture,
            FeatureStory featureStory) throws InvalidStoryException {
        final var tdds = new ArrayList<JiraTdd>();
        for (var tddId : featureStory.getTddReferences()) {
            var tdd = au.getTddContainersByComponent()
                    .stream()
                    .filter(container -> container.getTdds().containsKey(tddId) || TddId.noPr().equals(tddId))
                    .filter(container -> getComponentPath(beforeAuArchitecture, afterAuArchitecture, container).isPresent())
                    .map(container -> jiraTddFrom(
                            tddId,
                            container.getTdds().get(tddId),
                            getComponentPath(beforeAuArchitecture, afterAuArchitecture, container).orElseThrow(),
                            TddId.noPr().equals(tddId) ? null : container.getTdds().get(tddId).getContent()))
                    .findAny()
                    .orElseThrow(InvalidStoryException::new);
            tdds.add(tdd);
        }

        return tdds;
    }

    private static Optional<String> getComponentPath(
            ArchitectureDataStructure beforeAuArchitecture,
            ArchitectureDataStructure afterAuArchitecture,
            TddContainerByComponent tddContainerByComponent) {
        try {
            final ArchitectureDataStructure architecture;
            if (tddContainerByComponent.isDeleted())
                architecture = beforeAuArchitecture;
            else architecture = afterAuArchitecture;

            String id = tddContainerByComponent.getComponentId().toString();
            return Optional.of(
                    architecture.getModel().findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id))
                            .getPath()
                            .getPath()
            );
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class JiraTdd {
        private final TddId id;
        private final Tdd tdd;
        @Getter
        private final String componentPath;
        @Getter
        private final TddContent tddContent;

        public static JiraTdd jiraTddFrom(TddId id, Tdd tdd, String component, TddContent tddContent) {
            return new JiraTdd(id, tdd, component, tddContent);
        }

        public String getId() {
            return id.toString();
        }

        public String getText() {
            if (TddId.noPr().equals(id)) {
                return TddId.noPr().toString();
            }
            if (hasTddContent()) {
                return tddContent.getContent();
            } else {
                return tdd.getText();
            }
        }

        public boolean hasTddContent() {
            return tddContent != null;
        }
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class JiraFunctionalRequirement {
        private final FunctionalRequirementId id;
        private final FunctionalRequirement functionalRequirement;

        public String getId() {
            return id.toString();
        }

        public String getText() {
            return functionalRequirement.getText();
        }

        public String getSource() {
            return functionalRequirement.getSource();
        }
    }

    public static class InvalidStoryException extends Exception {
    }
}
