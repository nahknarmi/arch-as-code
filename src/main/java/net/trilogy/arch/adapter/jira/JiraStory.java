package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.YamlTddContainerByComponent;

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
    private final YamlFeatureStory featureStory;
    private final List<JiraTdd> tdds;
    private final List<JiraFunctionalRequirement> functionalRequirements;

    public JiraStory(YamlFeatureStory featureStory,
                     YamlArchitectureUpdate au,
                     ArchitectureDataStructure beforeAuArchitecture,
                     ArchitectureDataStructure afterAuArchitecture) throws InvalidStoryException {
        this.featureStory = featureStory;
        // TODO: More law of demeter: TDDs and FuncReqs
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

    private String makeTddTablesByComponent() {
        final var compMap = getTdds().stream()
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

    private String makeFunctionalRequirementTable() {
        return "h3. Implements functionality:\n" +
                "||Id||Source||Description||\n" +
                getFunctionalRequirements().stream()
                        .map(JiraStory::makeFunctionalRequirementRow)
                        .collect(joining());
    }

    public String makeDescription() {
        return makeFunctionalRequirementTable() + makeTddTablesByComponent();
    }

    /**
     * @todo Some Epic cards in JIRA seem to put the Epic Summary (title)
     * field into "customfield_10002"; manual testing shows that using the
     * card key (eg, "AU-1") works as well.  We should <strong>unit
     * test</strong> that AaC uses the key, not the title.
     * @todo Are "customfield_10002" and "customfield_10004" equivalent?
     */
    public IssueInput asNewIssueInput(
            String epicKey,
            Long projectId) {
        return new IssueInputBuilder()
                .setFieldValue("customfield_10002", epicKey)
                .setFieldValue("project", projectId)
                .setFieldValue("summary", featureStory.getTitle())
                .setFieldValue("issuetype", ComplexIssueInputFieldValue.with("name", "Feature Story"))
                .setFieldValue("description", makeDescription())
                .build();
    }

    /**
     * It is <strong>important</strong> to <em>not</em> provide the project ID
     * for an existing JIRA issue.  JIRA will complain at you if you try.
     */
    public IssueInput asExistingIssueInput(
            String epicKey) {
        return new IssueInputBuilder()
                .setFieldValue("customfield_10002", epicKey)
                .setFieldValue("summary", featureStory.getTitle())
                .setFieldValue("issuetype", ComplexIssueInputFieldValue.with("name", "Feature Story"))
                .setFieldValue("description", makeDescription())
                .build();
    }

    private static List<JiraFunctionalRequirement> getFunctionalRequirements(YamlArchitectureUpdate au, YamlFeatureStory featureStory) throws InvalidStoryException {
        final var requirements = new ArrayList<JiraFunctionalRequirement>();
        for (var reqId : featureStory.getRequirementReferences()) {
            if (!au.getFunctionalRequirements().containsKey(reqId))
                throw new InvalidStoryException();
            requirements.add(new JiraFunctionalRequirement(reqId, au.getFunctionalRequirements().get(reqId)));
        }
        return requirements;
    }

    private static List<JiraTdd> getTdds(
            YamlArchitectureUpdate au,
            ArchitectureDataStructure beforeAuArchitecture, ArchitectureDataStructure afterAuArchitecture,
            YamlFeatureStory featureStory) throws InvalidStoryException {
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
            YamlTddContainerByComponent tddContainerByComponent) {
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

    public String getKey() {
        return featureStory.getKey();
    }

    public String getLink() {
        return featureStory.getJira().getLink();
    }

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class JiraTdd {
        private final TddId id;
        private final YamlTdd tdd;
        @Getter
        private final String componentPath;
        @Getter
        private final TddContent tddContent;

        public static JiraTdd jiraTddFrom(TddId id, YamlTdd tdd, String component, TddContent tddContent) {
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
        private final YamlFunctionalRequirement functionalRequirement;

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
