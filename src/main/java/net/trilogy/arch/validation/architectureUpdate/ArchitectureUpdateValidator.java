package net.trilogy.arch.validation.architectureUpdate;

import io.vavr.collection.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.domain.c4.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forNotAvailableLink;

public class ArchitectureUpdateValidator {

    private final ArchitectureUpdate architectureUpdate;

    private final Set<String> allComponentIdsInBeforeArchitecture;
    private final Set<String> allComponentIdsInAfterArchitecture;

    private final Set<Tdd.Id> allTddIdsInStories;
    private final List<Tdd.Id> allTddIds;
    private final Set<FunctionalRequirement.Id> allFunctionalRequirementIds;
    private final Set<Tdd.Id> allTddIdsInDecisions;
    private final Set<Tdd.Id> allTddIdsInFunctionalRequirements;

    public static ValidationResult validate(
            ArchitectureUpdate architectureUpdateToValidate,
            ArchitectureDataStructure architectureAfterUpdate,
            ArchitectureDataStructure architectureBeforeUpdate) {

        return new ArchitectureUpdateValidator(
                architectureUpdateToValidate,
                architectureAfterUpdate,
                architectureBeforeUpdate
        ).run();
    }

    private ArchitectureUpdateValidator(
            ArchitectureUpdate architectureUpdate,
            ArchitectureDataStructure architectureAfterUpdate,
            ArchitectureDataStructure architectureBeforeUpdate) {
        this.architectureUpdate = architectureUpdate;

        allComponentIdsInBeforeArchitecture = getAllComponentIdsIn(architectureBeforeUpdate);
        allComponentIdsInAfterArchitecture = getAllComponentIdsIn(architectureAfterUpdate);

        allTddIdsInStories = getAllTddIdsReferencedByStories();
        allTddIds = getAllTddIds();
        allTddIdsInDecisions = getAllTddIdsReferencedByDecisions();
        allTddIdsInFunctionalRequirements = getAllTddIdsReferencedByFunctionalRequirements();
        allFunctionalRequirementIds = getAllFunctionalRequirementIds();
    }

    private ValidationResult run() {
        return new ValidationResult(Stream.concat(
                getErrors_DecisionsMustHaveTdds(),
                getErrors_DecisionsTddsMustBeValidReferences(),
                getErrors_FunctionalRequirementsTddsMustBeValidReferences(),

                getErrors_TddsMustHaveUniqueIds(),
                getErrors_ComponentsMustBeReferencedOnlyOnceForTdds(),

                getErrors_TddsComponentsMustBeValidReferences(),
                getErrors_TddsDeletedComponentsMustBeValidReferences(),

                getErrors_TddsMustHaveDecisionsOrRequirements(),

                getErrors_StoriesMustHaveFunctionalRequirements(),
                getErrors_StoriesFunctionalRequirementsMustBeValidReferences(),

                getErrors_StoriesMustHaveTdds(),
                getErrors_StoriesTddsMustBeValidReferences(),

                getErrors_TddsMustHaveStories(),
                getErrors_FunctionalRequirementsMustHaveStories(),
                getErrors_linksAreAvailable()
        ).collect(Collectors.toList()));
    }

    private Set<ValidationError> getErrors_linksAreAvailable() {
        List<Pair<String, String>> links = getAllLinksWithPath();

        return links.stream().filter(p -> p.getRight().equalsIgnoreCase("N/A")).map(p -> forNotAvailableLink(p.getLeft())).collect(toSet());
    }

    private List<Pair<String, String>> getAllLinksWithPath() {
        List<Pair<String, String>> links = new ArrayList<>();

        links.add(Pair.of("P1.link", architectureUpdate.getP1().getLink()));
        links.add(Pair.of("P1.jira.link", architectureUpdate.getP1().getJira().getLink()));
        links.add(Pair.of("P2.link", architectureUpdate.getP2().getLink()));
        links.add(Pair.of("P2.jira.link", architectureUpdate.getP2().getJira().getLink()));

        links.add(Pair.of("capabilities.epic.jira.link", architectureUpdate.getCapabilityContainer().getEpic().getJira().getLink()));


        List<MilestoneDependency> milestoneDependencies = architectureUpdate.getMilestoneDependencies();

        if (milestoneDependencies != null) {
            milestoneDependencies.forEach(m -> m.getLinks().forEach(l ->
                            links.add(Pair.of("Milestone dependency " + m.getDescription() + " link", l.getLink()))
                    )
            );
            milestoneDependencies.forEach(m -> m.getLinks().forEach(l ->
                            links.add(Pair.of("Milestone dependency " + m.getDescription() + " link", l.getLink()))
                    )
            );
        }
        architectureUpdate.getUsefulLinks().forEach(l ->
                links.add(Pair.of("Useful link " + l.getDescription() + " link", l.getLink()))
        );

        architectureUpdate.getCapabilityContainer().getFeatureStories().forEach(s ->
                links.add(Pair.of("capabilities.featurestory.jira.ticket " + s.getJira().getTicket() + " link", s.getJira().getLink()))
        );
        return links;
    }

    private Set<ValidationError> getErrors_ComponentsMustBeReferencedOnlyOnceForTdds() {
        var allComponentReferences = architectureUpdate.getTddContainersByComponent()
                .stream()
                .map(it -> new ComponentReferenceAndIsDeleted(it.getComponentId(), it.isDeleted()))
                .collect(Collectors.toList());
        return findDuplicates(allComponentReferences)
                .stream()
                .map(it -> ValidationError.forDuplicatedComponent(it.getComponentReference()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsMustHaveUniqueIds() {
        return findDuplicates(allTddIds).stream()
                .map(ValidationError::forDuplicatedTdd)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesFunctionalRequirementsMustBeValidReferences() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories()
                .stream()
                .filter(story -> story.getRequirementReferences() != null)
                .flatMap(story ->
                        getStoryRequirementReferencesStream(story)
                                .filter(funcReq -> !allFunctionalRequirementIds.contains(funcReq))
                                .map(funcReq -> ValidationError.forFunctionalRequirementsMustBeValidReferences(story.getTitle(), funcReq))
                )
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_FunctionalRequirementsMustHaveStories() {
        var storyReferencedFunctionalRequirements = getAllFunctionalRequirementsReferencedByStories();
        return architectureUpdate.getFunctionalRequirements().entrySet().stream()
                .filter(funcReqEntry -> !storyReferencedFunctionalRequirements.contains(funcReqEntry.getKey()))
                .map(funcReqEntry -> ValidationError.forMustHaveStories(funcReqEntry.getKey()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsComponentsMustBeValidReferences() {
        return architectureUpdate.getTddContainersByComponent()
                .stream()
                .filter(component -> !component.isDeleted())
                .map(TddContainerByComponent::getComponentId)
                .filter(componentReference ->
                        !allComponentIdsInAfterArchitecture.contains(componentReference.toString()))
                .map(ValidationError::forTddsComponentsMustBeValidReferences)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsDeletedComponentsMustBeValidReferences() {
        return architectureUpdate.getTddContainersByComponent()
                .stream()
                .filter(TddContainerByComponent::isDeleted)
                .map(TddContainerByComponent::getComponentId)
                .filter(componentReference ->
                        !allComponentIdsInBeforeArchitecture.contains(componentReference.toString()))
                .map(ValidationError::forDeletedTddsComponentsMustBeValidReferences)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsMustHaveDecisionsOrRequirements() {
        return allTddIds.stream()
                .filter(tddId -> !allTddIdsInFunctionalRequirements.contains(tddId))
                .filter(tddId -> !allTddIdsInDecisions.contains(tddId))
                .map(ValidationError::forTddsMustHaveDecisionsOrRequirements)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsMustHaveStories() {
        return allTddIds.stream()
                .filter(tdd -> !allTddIdsInStories.contains(tdd))
                .map(ValidationError::forMustHaveStories)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_DecisionsMustHaveTdds() {
        return architectureUpdate.getDecisions()
                .entrySet()
                .stream()
                .filter(decisionEntry -> decisionEntry.getValue().getTddReferences() == null || decisionEntry.getValue().getTddReferences().isEmpty())
                .map(decisionEntry -> ValidationError.forDecisionsMustHaveTdds(decisionEntry.getKey()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesMustHaveTdds() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories()
                .stream()
                .filter(story -> story.getTddReferences() == null || story.getTddReferences().isEmpty())
                .map(story -> ValidationError.forStoriesMustHaveTdds(story.getTitle()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesMustHaveFunctionalRequirements() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories()
                .stream()
                .filter(story -> story.getRequirementReferences() == null || story.getRequirementReferences().isEmpty())
                .map(story -> ValidationError.forStoriesMustHaveFunctionalRequirements(story.getTitle()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_DecisionsTddsMustBeValidReferences() {
        return architectureUpdate.getDecisions()
                .entrySet()
                .stream()
                .filter(decisionEntry -> decisionEntry.getValue().getTddReferences() != null)
                .flatMap(decisionEntry ->
                        decisionEntry.getValue().getTddReferences()
                                .stream()
                                .filter(tdd -> !allTddIds.contains(tdd))
                                .map(tdd -> ValidationError.forTddsMustBeValidReferences(decisionEntry.getKey(), tdd))
                )
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesTddsMustBeValidReferences() {
        return architectureUpdate.getCapabilityContainer()
                .getFeatureStories()
                .stream()
                .filter(story -> story.getTddReferences() != null)
                .flatMap(story ->
                        story.getTddReferences()
                                .stream()
                                .filter(tdd -> !allTddIds.contains(tdd))
                                .map(tdd -> ValidationError.forStoriesTddsMustBeValidReferences(tdd, story.getTitle()))
                )
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_FunctionalRequirementsTddsMustBeValidReferences() {
        return architectureUpdate.getFunctionalRequirements()
                .entrySet()
                .stream()
                .filter(functionalEntry -> functionalEntry.getValue().getTddReferences() != null)
                .flatMap(functionalEntry ->
                        functionalEntry.getValue().getTddReferences()
                                .stream()
                                .filter(tdd -> !allTddIds.contains(tdd))
                                .map(tdd -> ValidationError.forTddsMustBeValidReferences(functionalEntry.getKey(), tdd)))
                .collect(toSet());
    }

    private List<Tdd.Id> getAllTddIds() {
        return architectureUpdate.getTddContainersByComponent()
                .stream()
                .flatMap(container -> container.getTdds().keySet().stream())
                .collect(Collectors.toList());
    }

    private Set<FunctionalRequirement.Id> getAllFunctionalRequirementIds() {
        return architectureUpdate.getFunctionalRequirements().keySet();
    }

    private Set<Tdd.Id> getAllTddIdsReferencedByStories() {
        return architectureUpdate.getCapabilityContainer()
                .getFeatureStories()
                .stream()
                .flatMap(story -> story.getTddReferences().stream())
                .collect(toSet());
    }

    private Set<Tdd.Id> getAllTddIdsReferencedByFunctionalRequirements() {
        return architectureUpdate.getFunctionalRequirements()
                .values()
                .stream()
                .filter(requirement -> requirement.getTddReferences() != null)
                .flatMap(requirement -> requirement.getTddReferences().stream())
                .collect(toSet());
    }

    private Set<FunctionalRequirement.Id> getAllFunctionalRequirementsReferencedByStories() {
        return architectureUpdate.getCapabilityContainer()
                .getFeatureStories().stream()
                .flatMap(story -> getStoryRequirementReferencesStream(story))
                .collect(toSet());
    }

    private java.util.stream.Stream<FunctionalRequirement.Id> getStoryRequirementReferencesStream(FeatureStory story) {
        if (story.getRequirementReferences() == null)
            return java.util.stream.Stream.empty();

        return story.getRequirementReferences().stream();
    }

    private Set<Tdd.Id> getAllTddIdsReferencedByDecisions() {
        return architectureUpdate.getDecisions()
                .values()
                .stream()
                .filter(decision -> decision.getTddReferences() != null)
                .flatMap(decision -> decision.getTddReferences().stream())
                .collect(toSet());
    }

    private Set<String> getAllComponentIdsIn(ArchitectureDataStructure architecture) {
        return architecture.getModel().getComponents().stream().map(Entity::getId).collect(toSet());
    }

    private <T> Set<T> findDuplicates(Collection<T> collection) {
        Set<T> uniques = new HashSet<>();
        return collection
                .stream()
                .filter(t -> !uniques.add(t))
                .collect(toSet());
    }

    @EqualsAndHashCode
    @Getter
    @RequiredArgsConstructor
    private static class ComponentReferenceAndIsDeleted {
        private final Tdd.ComponentReference componentReference;
        private final boolean isDeleted;
    }
}
