package net.trilogy.arch.validation.architectureUpdate;

import lombok.Data;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.MilestoneDependency;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.Entity;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static io.vavr.collection.Stream.concat;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forAmbiguousTddContentReference;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forComponentPathNotMatchingId;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forDecisionsMustHaveTdds;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forDuplicatedComponent;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forFunctionalRequirementsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forNoPrWithAnotherTdd;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forNotAvailableLink;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forOverriddenByTddContentFile;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forStoriesMustHaveFunctionalRequirements;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forStoriesMustHaveTdds;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forStoriesTddsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forTddsMustBeValidReferences;

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
                architectureBeforeUpdate)
                .run();
    }

    private static boolean tddReferenceCombinedWithNoPr(List<TddId> refs) {
        return refs != null && refs.contains(TddId.noPr()) && refs.size() > 1;
    }

    private static ValidationError createAmbiguousTddContentReferenceValidationError(TddContainerByComponent tddContainerByComponent, Map.Entry<TddId, Tdd> pair) {
        TddId id = pair.getKey();
        Tdd tdd = pair.getValue();

        boolean tddContentIsEmpty = (tdd.getText() == null || tdd.getText().isEmpty()) && (tdd.getFile() == null || tdd.getFile().isEmpty());
        if (tddContentIsEmpty) return null;

        boolean errorCondition = tdd.getText() != null &&
                tdd.getFile() != null &&
                !tdd.getText().isEmpty() &&
                !tdd.getFile().isEmpty();

        if (errorCondition) {
            return forAmbiguousTddContentReference(tddContainerByComponent.getComponentId(), id);
        }

        return null;
    }

    private static java.util.stream.Stream<TddId> getTddReferencesStream(FeatureStory story) {
        if (story.getTddReferences() == null)
            return java.util.stream.Stream.empty();

        return story.getTddReferences().stream();
    }

    private static java.util.stream.Stream<FunctionalRequirementId> getStoryRequirementReferencesStream(FeatureStory story) {
        if (story.getRequirementReferences() == null)
            return java.util.stream.Stream.empty();

        return story.getRequirementReferences().stream();
    }

    private static Set<String> getAllComponentIdsIn(ArchitectureDataStructure architecture) {
        return architecture.getModel().getComponents().stream().map(Entity::getId).collect(toSet());
    }

    /**
     * @todo There are better ways, such as a library, or removing altogether
     */
    private static <T> Set<T> findDuplicates(Collection<T> collection) {
        final var uniques = new HashSet<T>();
        return collection.stream()
                .filter(t -> !uniques.add(t))
                .collect(toSet());
    }

    private ValidationResult run() {
        return new ValidationResult(concat(
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
                getErrors_TddTextAndContentsFileExists(),

                getErrors_ComponentPathMatchingId())
                .collect(toList()));
    }

    private Set<ValidationError> getErrors_ComponentPathMatchingId() {
        return architectureUpdate.getTddContainersByComponent().stream()
                .filter(c -> c.getComponentId() != null && c.getComponentPath() != null)
                .filter(c -> {
                    final var c4Component = allComponents.stream().filter(c4 -> c4.getId().equals(c.getComponentId().getId())).findFirst();
                    if (c4Component.isEmpty() || c4Component.get().getPath() == null)
                        return false;
                    return !c4Component.get().getPath().getPath().equalsIgnoreCase(c.getComponentPath());
                })
                .map(c -> forComponentPathNotMatchingId(c.getComponentId().getId()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_NoPrNotCombinedWithAnotherTddId() {
        Stream<ValidationError> decisionErrors = architectureUpdate.getDecisions().values().stream()
                .filter(d -> tddReferenceCombinedWithNoPr(d.getTddReferences()))
                .map(d -> forNoPrWithAnotherTdd("Decision " + d.getText()));
        Stream<ValidationError> requirementErrors = architectureUpdate.getFunctionalRequirements().values().stream()
                .filter(r -> tddReferenceCombinedWithNoPr(r.getTddReferences()))
                .map(r -> forNoPrWithAnotherTdd("Functional requirement " + r.getText()));
        Stream<ValidationError> storyErrors = architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .filter(s -> tddReferenceCombinedWithNoPr(s.getTddReferences()))
                .map(s -> forNoPrWithAnotherTdd("Feature story " + s.getTitle()));

        return concat(concat(decisionErrors, requirementErrors), storyErrors).collect(toSet());
    }

    private Set<ValidationError> getErrors_LinksAreAvailable() {
        return getAllLinksWithPath().stream()
                .filter(p -> p.getRight() == null || p.getRight().equalsIgnoreCase("N/A"))
                .map(p -> forNotAvailableLink(p.getLeft()))
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
                    links.add(Pair.of("Milestone dependency " + m.getDescription() + " link", l.getLink()))));
            milestoneDependencies.forEach(m -> m.getLinks().forEach(l ->
                    links.add(Pair.of("Milestone dependency " + m.getDescription() + " link", l.getLink()))));
        }
        architectureUpdate.getUsefulLinks().forEach(l ->
                links.add(Pair.of("Useful link " + l.getDescription() + " link", l.getLink())));

        architectureUpdate.getCapabilityContainer().getFeatureStories().stream().filter(s -> s.getJira().getLink() != null).forEach(s ->
                links.add(Pair.of("capabilities.featurestory.jira.ticket " + s.getJira().getTicket() + " link", s.getJira().getLink())));

        return links;
    }

    private Set<ValidationError> getErrors_ComponentsMustBeReferencedOnlyOnceForTdds() {
        var allComponentReferences = architectureUpdate.getTddContainersByComponent()
                .stream()
                .map(it -> new ComponentReferenceAndIsDeleted(it.getComponentId(), it.isDeleted()))
                .collect(toList());
        return findDuplicates(allComponentReferences)
                .stream()
                .map(it -> forDuplicatedComponent(it.getComponentReference()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsMustHaveUniqueIds() {
        return findDuplicates(allTddIds).stream()
                .map(ValidationError::forDuplicatedTdd)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesFunctionalRequirementsMustBeValidReferences() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .filter(story -> story.getRequirementReferences() != null)
                .flatMap(story ->
                        getStoryRequirementReferencesStream(story)
                                .filter(funcReq -> !allFunctionalRequirementIds.contains(funcReq))
                                .map(funcReq -> forFunctionalRequirementsMustBeValidReferences(story.getTitle(), funcReq)))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_FunctionalRequirementsMustHaveStories() {
        final var storyReferencedFunctionalRequirements = getAllFunctionalRequirementsReferencedByStories();

        return architectureUpdate.getFunctionalRequirements().keySet().stream()
                .filter(functionalRequirement -> !storyReferencedFunctionalRequirements.contains(functionalRequirement))
                .map(ValidationError::forMustHaveStories)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsComponentsMustBeValidReferences() {
        return architectureUpdate.getTddContainersByComponent().stream()
                .filter(component -> !component.isDeleted())
                .map(TddContainerByComponent::getComponentId)
                .filter(componentReference ->
                        !allComponentIdsInAfterArchitecture.contains(componentReference.toString()))
                .map(ValidationError::forTddsComponentsMustBeValidReferences)
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_TddsDeletedComponentsMustBeValidReferences() {
        return architectureUpdate.getTddContainersByComponent().stream()
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
                .map(decisionEntry -> forDecisionsMustHaveTdds(decisionEntry.getKey()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesMustHaveTdds() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .filter(story -> story.getTddReferences() == null || story.getTddReferences().isEmpty())
                .map(story -> forStoriesMustHaveTdds(story.getTitle()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesMustHaveFunctionalRequirements() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .filter(story -> story.getRequirementReferences() == null || story.getRequirementReferences().isEmpty())
                .map(story -> forStoriesMustHaveFunctionalRequirements(story.getTitle()))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_DecisionsTddsMustBeValidReferences() {
        return architectureUpdate.getDecisions().entrySet().stream()
                .filter(decisionEntry -> decisionEntry.getValue().getTddReferences() != null)
                .flatMap(decisionEntry ->
                        decisionEntry.getValue().getTddReferences()
                                .stream()
                                .filter(tdd -> !allTddIds.contains(tdd) && !TddId.noPr().equals(tdd))
                                .map(tdd -> forTddsMustBeValidReferences(decisionEntry.getKey(), tdd)))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_StoriesTddsMustBeValidReferences() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .filter(story -> story.getTddReferences() != null)
                .flatMap(story ->
                        getTddReferencesStream(story)
                                .filter(tdd -> !allTddIds.contains(tdd) && !TddId.noPr().equals(tdd))
                                .map(tdd -> forStoriesTddsMustBeValidReferences(tdd, story.getTitle())))
                .collect(toSet());
    }

    private Set<ValidationError> getErrors_FunctionalRequirementsTddsMustBeValidReferences() {
        return architectureUpdate.getFunctionalRequirements().entrySet().stream()
                .filter(functionalEntry -> functionalEntry.getValue().getTddReferences() != null)
                .flatMap(functionalEntry ->
                        functionalEntry.getValue().getTddReferences()
                                .stream()
                                .filter(tdd -> !allTddIds.contains(tdd) && !TddId.noPr().equals(tdd))
                                .map(tdd -> forTddsMustBeValidReferences(functionalEntry.getKey(), tdd)))
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

    private Set<ValidationError> getErrors_TddTextAndContentsFileExists() {
        return architectureUpdate.getTddContainersByComponent().stream()
                .flatMap(tddContainer -> tddContainer.getTdds().entrySet().stream()
                        .map(pair -> {
                            TddId id = pair.getKey();
                            Tdd tdd = pair.getValue();

                            // Error condition: Text exists and is overridden by found matching file.
                            boolean errorCondition = tdd.getContent() != null && tdd.getText() != null;
                            if (errorCondition) {
                                return forOverriddenByTddContentFile(tddContainer.getComponentId(), id, tdd.getContent().getFilename());
                            }
                            return null;
                        }))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private List<TddId> getAllTddIds() {
        Stream<TddId> tddIds = architectureUpdate.getTddContainersByComponent()
                .stream()
                .flatMap(container -> container.getTdds().keySet().stream());

        return concat(tddIds, Stream.of(TddId.noPr()))
                .collect(toList());
    }

    private Set<FunctionalRequirementId> getAllFunctionalRequirementIds() {
        return architectureUpdate.getFunctionalRequirements().keySet();
    }

    private Set<TddId> getAllTddIdsReferencedByStories() {
        return architectureUpdate.getCapabilityContainer().getFeatureStories().stream()
                .flatMap(ArchitectureUpdateValidator::getTddReferencesStream)
                .collect(toSet());
    }

    private Set<TddId> getAllTddIdsReferencedByFunctionalRequirements() {
        return architectureUpdate.getFunctionalRequirements().values().stream()
                .filter(requirement -> requirement.getTddReferences() != null)
                .flatMap(requirement -> requirement.getTddReferences().stream())
                .collect(toSet());
    }

    private Set<FunctionalRequirementId> getAllFunctionalRequirementsReferencedByStories() {
        return architectureUpdate.getCapabilityContainer()
                .getFeatureStories().stream()
                .flatMap(ArchitectureUpdateValidator::getStoryRequirementReferencesStream)
                .collect(toSet());
    }

    private Set<TddId> getAllTddIdsReferencedByDecisions() {
        return architectureUpdate.getDecisions().values().stream()
                .filter(decision -> decision.getTddReferences() != null)
                .flatMap(decision -> decision.getTddReferences().stream())
                .collect(toSet());
    }

    @Data
    private static class ComponentReferenceAndIsDeleted {
        private final TddComponentReference componentReference;
        private final boolean isDeleted;
    }
}
