package net.trilogy.arch.adapter.jira;

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
            FeatureStory featureStory
    ) throws InvalidStoryException {

        List<JiraTdd> tdds = new ArrayList<>();
        for (var tddId : featureStory.getTddReferences()) {
            var tdd = au.getTddContainersByComponent()
                    .stream()
                    .filter(container -> container.getTdds().containsKey(tddId))
                    .filter(container -> getComponentPath(beforeAuArchitecture, afterAuArchitecture, container).isPresent())
                    .map(container -> jiraTddFrom(
                            tddId,
                            container.getTdds().get(tddId),
                            getComponentPath(beforeAuArchitecture, afterAuArchitecture, container).orElseThrow(),
                            container.getTdds().get(tddId).getContent()))
                    .findAny()
                    .orElseThrow(InvalidStoryException::new);
            tdds.add(tdd);
        }

        return tdds;
    }

    private static Optional<String> getComponentPath(ArchitectureDataStructure beforeAuArchitecture,
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
