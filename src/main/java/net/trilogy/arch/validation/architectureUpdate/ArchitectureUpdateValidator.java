package net.trilogy.arch.validation.architectureUpdate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.MilestoneDependency;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ArchitectureUpdateValidator {

    private final ArchitectureUpdate architectureUpdate;

    private final Set<String> allComponentIdsInBeforeArchitecture;
    private final Set<String> allComponentIdsInAfterArchitecture;

    private final Set<TddId> allTddIdsInStories;
    private final List<TddId> allTddIds;
    private final Set<FunctionalRequirementId> allFunctionalRequirementIds;
    private final Set<TddId> allTddIdsInDecisions;
    private final Set<TddId> allTddIdsInFunctionalRequirements;
    private final Set<C4Component> allComponents;

    private ArchitectureUpdateValidator(
            ArchitectureUpdate architectureUpdate,
            ArchitectureDataStructure architectureAfterUpdate,
            ArchitectureDataStructure architectureBeforeUpdate) {
        this.architectureUpdate = architectureUpdate;

        allComponentIdsInBeforeArchitecture = getAllComponentIdsIn(architectureBeforeUpdate);
        allComponentIdsInAfterArchitecture = getAllComponentIdsIn(architectureAfterUpdate);

        allComponents = architectureAfterUpdate.getModel().getComponents();
        allComponents.addAll(architectureBeforeUpdate.getModel().getComponents());

        allTddIdsInStories = getAllTddIdsReferencedByStories();
        allTddIds = getAllTddIds();
        allTddIdsInDecisions = getAllTddIdsReferencedByDecisions();
        allTddIdsInFunctionalRequirements = getAllTddIdsReferencedByFunctionalRequirements();
        allFunctionalRequirementIds = getAllFunctionalRequirementIds();
    }

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

    private ValidationResult run() {
        return new ValidationResult(io.vavr.collection.Stream.concat(
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
                getErrors_NoPrNotCombinedWithAnotherTddId(),
                getErrors_LinksAreAvailable(),

                getErrors_OnlyOneTddContentsReference(),
                getErrors_TddContentsFileExists(),
                getErrors_TddsMustHaveOnlyOneTddContentFile(),

                getErrors_ComponentPathMatchingId()
        ).collect(toList()));
    }

    private Set<ValidationError> getErrors_ComponentPathMatchingId() {
        return architectureUpdate.getTddContainersByComponent().stream()
                .filter(c -> c.getComponentId() != null && c.getComponentPath() != null)
                .filter(c -> {
                    Optional<C4Component> c4Component = allComponents.stream().filter(c4 -> c4.getId().equals(c.getComponentId().getId())).findFirst();
                    if (c4Component.isEmpty() || c4Component.get().getPath() == null) return false;
                    return ! c4Component.get().getPath().getPath().equalsIgnoreCase(c.getComponentPath());
                }).map(c -> ValidationError.forComponentPathNotMatchingId(c.getComponentId().getId())).collect(toSet());
    }

    private Set<ValidationError> getErrors_NoPrNotCombinedWithAnotherTddId() {
        Stream<ValidationError> decisionErrors = architectureUpdate.getDecisions().values().stream()
                .filter(d -> tddReferenceCombinedWithNoPr(d.getTddReferences()))
                .map(d -> ValidationError.forNoPrWithAnotherTdd("Decision " + d.getText()));
        Stream<ValidationError> requirementErrors = architectureUpdate.getFunctionalRequirements().values().stream()
                .filter(r -> tddReferenceCombinedWithNoPr(r.getTddReferences()))
                .map(r -> ValidationError.forNoPrWithAnotherTdd("Functional requirement " + r.getText()));
        Stream<ValidationError> storyErrors = architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .filter(s -> tddReferenceCombinedWithNoPr(s.getTddReferences()))
                .map(s -> ValidationError.forNoPrWithAnotherTdd("Feature story " + s.getTitle()));
        return Stream.concat(Stream.concat(decisionErrors, requirementErrors), storyErrors).collect(toSet());
    }

    private boolean tddReferenceCombinedWithNoPr(List<TddId> refs) {
        return refs != null && refs.contains(TddId.noPr()) && refs.size() > 1;
    }

    private Set<ValidationError> getErrors_LinksAreAvailable() {
        return getAllLinksWithPath().stream()
                .filter(p -> p.getRight() == null || p.getRight().equalsIgnoreCase("N/A"))
                .map(p -> ValidationError.forNotAvailableLink(p.getLeft()))
                .collect(toSet());
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

        architectureUpdate.getCapabilityContainer().getFeatureStories().stream().filter(s -> s.getJira().getLink() != null ).forEach(s ->
                links.add(Pair.of("capabilities.featurestory.jira.ticket " + s.getJira().getTicket() + " link", s.getJira().getLink()))
        );
        return links;
    }

    private Set<ValidationError> getErrors_ComponentsMustBeReferencedOnlyOnceForTdds() {
        var allComponentReferences = architectureUpdate.getTddContainersByComponent()
                .stream()
                .map(it -> new ComponentReferenceAndIsDeleted(it.getComponentId(), it.isDeleted()))
                .collect(toList());
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
        return architectureUpdate.getFunctionalRequirements().keySet().stream()
                .filter(functionalRequirement -> !storyReferencedFunctionalRequirements.contains(functionalRequirement))
                .map(ValidationError::forMustHaveStories)
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
                .filter(tddId -> !allTddIdsInFunctionalRequirements.contains(tddId) && !TddId.noPr().equals(tddId))
                .filter(tddId -> !allTddIdsInDecisions.contains(tddId) && !TddId.noPr().equals(tddId))
                .map(ValidationError::forTddsMustHaveDecisionsOrRequirements)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsMustHaveStories() {
        return allTddIds.stream()
                .filter(tdd -> !allTddIdsInStories.contains(tdd) && !TddId.noPr().equals(tdd))
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
                                .filter(tdd -> !allTddIds.contains(tdd) && !TddId.noPr().equals(tdd))
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
                        getTddReferencesStream(story)
                                .filter(tdd -> !allTddIds.contains(tdd) && !TddId.noPr().equals(tdd))
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
                                .filter(tdd -> !allTddIds.contains(tdd) && !TddId.noPr().equals(tdd))
                                .map(tdd -> ValidationError.forTddsMustBeValidReferences(functionalEntry.getKey(), tdd)))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_OnlyOneTddContentsReference() {
        return architectureUpdate.getTddContainersByComponent().stream()
                .map(tddContainerByComponent -> tddContainerByComponent.getTdds().entrySet().stream()
                        .map(pair -> createAmbiguousTddContentReferenceValidationError(tddContainerByComponent, pair))
                        .collect(toSet()))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddContentsFileExists() {
        List<TddContent> tddContents = architectureUpdate.getTddContents();
        if (tddContents == null || tddContents.isEmpty()) return Set.of();

        var tddGroupings = tddContents.stream()
                .collect(groupingBy(TddContent::getTdd));

        var tddFilename = tddContents.stream()
                // Duplicate keys are ignored because they are caught by getErrors_TddsMustHaveOnlyOneTddContentFile()
                .filter(tc -> tddGroupings.get(tc.getTdd()).size() == 1)
                .collect(toMap(TddContent::getTdd, TddContent::getFilename));

        return architectureUpdate.getTddContainersByComponent().stream()
                .flatMap(tddContainer -> tddContainer.getTdds().entrySet().stream()
                        .map(pair -> {
                            TddId id = pair.getKey();
                            Tdd tdd = pair.getValue();

                            // Error condition: Text exists and is overridden by found matching file.
                            boolean errorCondition = tddFilename.containsKey(id.toString()) && tdd.getText() != null;
                            if (errorCondition) {
                                return ValidationError.forOverriddenByTddContentFile(tddContainer.getComponentId(), id, tddFilename.get(id.toString()));
                            }
                            return null;
                        })
                ).filter(Objects::nonNull)
                .collect(toSet());
    }

    private ValidationError createAmbiguousTddContentReferenceValidationError(TddContainerByComponent tddContainerByComponent, Map.Entry<TddId, Tdd> pair) {
        TddId id = pair.getKey();
        Tdd tdd = pair.getValue();

        boolean tddContentIsEmpty = (tdd.getText() == null || tdd.getText().isEmpty()) && (tdd.getFile() == null || tdd.getFile().isEmpty());
        if (tddContentIsEmpty) return null;

        boolean errorCondition =
                tdd.getText() != null &&
                        tdd.getFile() != null &&
                        !tdd.getText().isEmpty() &&
                        !tdd.getFile().isEmpty();
        if (errorCondition) {
            return ValidationError.forAmbiguousTddContentReference(tddContainerByComponent.getComponentId(), id);
        }

        return null;
    }

    private Set<ValidationError> getErrors_TddsMustHaveOnlyOneTddContentFile() {
        List<TddContent> tddContentFiles = architectureUpdate.getTddContents();
        if (tddContentFiles == null || tddContentFiles.isEmpty())
            return Set.of();

        return tddContentFiles.stream()
                .collect(groupingBy((tc) -> Pair.of(tc.getTdd(), tc.getComponentId())))
                .entrySet().stream()
                .filter(es -> es.getValue().size() > 1)
                .map(es -> {
                    var pair = es.getKey();
                    List<TddContent> tddContents = es.getValue();
                    String tddId = pair.getLeft();
                    String componentId = pair.getRight();

                    return ValidationError.forMultipleTddContentFilesForTdd(new TddComponentReference(componentId), new TddId(tddId), tddContents);
                }).collect(toSet());
    }

    private List<TddId> getAllTddIds() {
        Stream<TddId> tddIds = architectureUpdate.getTddContainersByComponent()
                .stream()
                .flatMap(container -> container.getTdds().keySet().stream());
        return Stream.concat(
                tddIds,
                Stream.of(TddId.noPr()))
                .collect(toList());
    }

    private Set<FunctionalRequirementId> getAllFunctionalRequirementIds() {
        return architectureUpdate.getFunctionalRequirements().keySet();
    }

    private Set<TddId> getAllTddIdsReferencedByStories() {
        return architectureUpdate.getCapabilityContainer()
                .getFeatureStories()
                .stream()
                .flatMap(this::getTddReferencesStream)
                .collect(toSet());
    }

    private Set<TddId> getAllTddIdsReferencedByFunctionalRequirements() {
        return architectureUpdate.getFunctionalRequirements()
                .values()
                .stream()
                .filter(requirement -> requirement.getTddReferences() != null)
                .flatMap(requirement -> requirement.getTddReferences().stream())
                .collect(toSet());
    }

    private Set<FunctionalRequirementId> getAllFunctionalRequirementsReferencedByStories() {
        return architectureUpdate.getCapabilityContainer()
                .getFeatureStories().stream()
                .flatMap(this::getStoryRequirementReferencesStream)
                .collect(toSet());
    }

    private java.util.stream.Stream<TddId> getTddReferencesStream(FeatureStory story) {
        if (story.getTddReferences() == null)
            return java.util.stream.Stream.empty();

        return story.getTddReferences().stream();
    }

    private java.util.stream.Stream<FunctionalRequirementId> getStoryRequirementReferencesStream(FeatureStory story) {
        if (story.getRequirementReferences() == null)
            return java.util.stream.Stream.empty();

        return story.getRequirementReferences().stream();
    }

    private Set<TddId> getAllTddIdsReferencedByDecisions() {
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
        private final TddComponentReference componentReference;
        private final boolean isDeleted;
    }
}
