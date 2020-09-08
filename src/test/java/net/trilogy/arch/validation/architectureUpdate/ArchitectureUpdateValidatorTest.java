package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.CapabilitiesContainer;
import net.trilogy.arch.domain.architectureUpdate.Decision;
import net.trilogy.arch.domain.architectureUpdate.Decision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.Epic;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.domain.architectureUpdate.Link;
import net.trilogy.arch.domain.architectureUpdate.MilestoneDependency;
import net.trilogy.arch.domain.architectureUpdate.P1;
import net.trilogy.arch.domain.architectureUpdate.P2;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE;
import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_BEFORE_UPDATE;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.builderPreFilledWithBlanks;
import static net.trilogy.arch.validation.architectureUpdate.ArchitectureUpdateValidator.validate;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forAmbiguousTddContentReference;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forComponentPathNotMatchingId;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forDecisionsMustHaveTdds;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forDeletedTddsComponentsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forDuplicatedComponent;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forDuplicatedTdd;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forFunctionalRequirementsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forMultipleTddContentFilesForTdd;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forMustHaveStories;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forNotAvailableLink;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forOverriddenByTddContentFile;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forStoriesMustHaveFunctionalRequirements;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forStoriesMustHaveTdds;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forStoriesTddsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forTddsComponentsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forTddsMustBeValidReferences;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forTddsMustHaveDecisionsOrRequirements;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ArchitectureUpdateValidatorTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private ArchitectureDataStructure validDataStructure;
    private ArchitectureDataStructure hasMissingComponentDataStructure;

    @Before
    public void setUp() throws IOException {
        final var validArchAsString = new FilesFacade().readString(new File(
                getClass().getResource(MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE).getPath())
                .toPath());
        validDataStructure = YAML_OBJECT_MAPPER.readValue(validArchAsString, ArchitectureDataStructure.class);

        final var missingComponentArchAsString = new FilesFacade().readString(new File(
                getClass().getResource(MANIFEST_PATH_TO_TEST_AU_VALIDATION_BEFORE_UPDATE).getPath())
                .toPath());
        hasMissingComponentDataStructure = YAML_OBJECT_MAPPER.readValue(missingComponentArchAsString, ArchitectureDataStructure.class);
    }

    @Test
    public void blankAuShouldBeValid() {
        var result = validate(ArchitectureUpdate.blank(), validDataStructure, validDataStructure);

        collector.checkThat(result.isValid(), is(true));
        collector.checkThat(result.isValid(ValidationStage.STORY), is(true));
        collector.checkThat(result.isValid(ValidationStage.TDD), is(true));
    }

    @Test
    public void shouldValidate_DecisionsMustHaveTdds() {
        var invalidAu = builderPreFilledWithBlanks().decisions(Map.of(
                new DecisionId("Null TDD references"), new Decision("[SAMPLE DECISION TEXT]", null),
                new DecisionId("Empty TDD references"), new Decision("Decision Text", List.of())))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forDecisionsMustHaveTdds(new DecisionId("Null TDD references")),
                forDecisionsMustHaveTdds(new DecisionId("Empty TDD references")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_DecisionsTddsMustBeValidReferences() {
        var invalidAu = builderPreFilledWithBlanks().decisions(Map.of(
                new DecisionId("Bad-TDD-Decision"),
                new Decision("Decision Text", List.of(new TddId("BAD-TDD-ID")))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        collector.checkThat(actualErrors,
                hasItem(forTddsMustBeValidReferences(new DecisionId("Bad-TDD-Decision"), new TddId("BAD-TDD-ID"))));
    }

    @Test
    public void shouldValidate_FunctionalRequirementsTddsMustBeValidReferences() {
        var invalidAu = builderPreFilledWithBlanks().functionalRequirements(Map.of(
                new FunctionalRequirementId("Bad-TDD-Functional-Requirement"),
                new FunctionalRequirement("Text", "Source", List.of(
                        new TddId("BAD-TDD-ID-1"),
                        new TddId("BAD-TDD-ID-2")
                ))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsMustBeValidReferences(new FunctionalRequirementId("Bad-TDD-Functional-Requirement"), new TddId("BAD-TDD-ID-1")),
                forTddsMustBeValidReferences(new FunctionalRequirementId("Bad-TDD-Functional-Requirement"), new TddId("BAD-TDD-ID-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsMustHaveUniqueIds() {
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("1"),
                        null, true,
                        Map.of(new TddId("Dupe-1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("1"),
                        null, true,
                        Map.of(new TddId("Dupe-1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("1"),
                        null, true,
                        Map.of(new TddId("Dupe-2"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("1"),
                        null, true,
                        Map.of(new TddId("Dupe-2"), new Tdd("text", null)))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forDuplicatedTdd(new TddId("Dupe-1")),
                forDuplicatedTdd(new TddId("Dupe-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_ComponentsMustBeReferencedOnlyOnceForTdds() {
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("Dupe-1"),
                        null, false,
                        Map.of(new TddId("1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("Dupe-1"),
                        null, false,
                        Map.of(new TddId("2"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("Dupe-2"),
                        null, false,
                        Map.of(new TddId("3"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("Dupe-2"),
                        null, false,
                        Map.of(new TddId("4"), new Tdd("text", null)))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forDuplicatedComponent(new TddComponentReference("Dupe-1")),
                forDuplicatedComponent(new TddComponentReference("Dupe-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsComponentsMustBeValidReferences() {
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("Non-existent-1"),
                        null, false,
                        Map.of(new TddId("1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("Non-existent-2"),
                        null, false,
                        Map.of(new TddId("2"), new Tdd("text", null)))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsComponentsMustBeValidReferences(new TddComponentReference("Non-existent-1")),
                forTddsComponentsMustBeValidReferences(new TddComponentReference("Non-existent-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsDeletedComponentsMustBeValidReferences() {
        // Given
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("Non-existent-deleted-1"),
                        null, true,
                        Map.of(new TddId("1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("Non-existent-deleted-2"),
                        null, true,
                        Map.of(new TddId("2"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("Valid-Deleted-Component-Id"),  // Present in beforeUpdate Architecture
                        null, true,
                        Map.of(new TddId("3"), new Tdd("text", null)))))
                .build();

        // When
        var actualErrors = validate(invalidAu, validDataStructure, hasMissingComponentDataStructure).getErrors();

        // Then
        var expectedErrors = List.of(
                forDeletedTddsComponentsMustBeValidReferences(new TddComponentReference("Non-existent-deleted-1")),
                forDeletedTddsComponentsMustBeValidReferences(new TddComponentReference("Non-existent-deleted-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));

        // And ensure that the valid deleted component is not present in errors
        collector.checkThat(actualErrors,
                not(hasItem(forDeletedTddsComponentsMustBeValidReferences(
                        new TddComponentReference("Valid-Deleted-Component-Id")
                ))));
    }

    @Test
    public void shouldValidate_TddsMustHaveDecisionsOrRequirements() {
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),
                        null, false,
                        Map.of(new TddId("No-decision-or-req-1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),
                        null, true,
                        Map.of(new TddId("No-decision-or-req-2"), new Tdd("text", null)))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsMustHaveDecisionsOrRequirements(new TddId("No-decision-or-req-1")),
                forTddsMustHaveDecisionsOrRequirements(new TddId("No-decision-or-req-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_StoriesMustHaveFunctionalRequirements() {
        var invalidAu = builderPreFilledWithBlanks().capabilityContainer(new CapabilitiesContainer(
                Epic.blank(),
                List.of(
                        new FeatureStory(
                                "Feat Title 1", Jira.blank(), List.of(TddId.blank()),
                                List.of()),
                        new FeatureStory("Feat Title 2", Jira.blank(), List.of(TddId.blank()),
                                null))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forStoriesMustHaveFunctionalRequirements("Feat Title 1"),
                forStoriesMustHaveFunctionalRequirements("Feat Title 2"));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValiate_StoriesFunctionalRequirementsMustBeValidReferences() {
        var invalidAu = builderPreFilledWithBlanks().capabilityContainer(new CapabilitiesContainer(
                Epic.blank(),
                List.of(
                        new FeatureStory(
                                "Feat Title 1", Jira.blank(), List.of(TddId.blank()),
                                List.of(new FunctionalRequirementId("Invalid-Functional-Requirement"))))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forFunctionalRequirementsMustBeValidReferences("Feat Title 1", new FunctionalRequirementId("Invalid-Functional-Requirement")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_StoriesMustHaveTdds() {
        var invalidAu = builderPreFilledWithBlanks().capabilityContainer(new CapabilitiesContainer(
                Epic.blank(),
                List.of(
                        new FeatureStory(
                                "Feat Title 1", Jira.blank(),
                                List.of(), // Empty TDD reference
                                List.of()),
                        new FeatureStory(
                                "Feat Title 2", Jira.blank(),
                                null, // Null TDD reference
                                List.of()))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forStoriesMustHaveTdds("Feat Title 1"),
                forStoriesMustHaveTdds("Feat Title 2"));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_StoriesTddsMustBeValidReferences() {
        var invalidAu = builderPreFilledWithBlanks().capabilityContainer(new CapabilitiesContainer(
                Epic.blank(),
                List.of(new FeatureStory(
                        "Feat Title 1", Jira.blank(),
                        List.of(new TddId("Invalid TDD 1"),
                                new TddId("Invalid TDD 2")),
                        List.of()))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forStoriesTddsMustBeValidReferences(new TddId("Invalid TDD 1"), "Feat Title 1"),
                forStoriesTddsMustBeValidReferences(new TddId("Invalid TDD 2"), "Feat Title 1"));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsMustHaveStories() {
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),
                        null, false,
                        Map.of(new TddId("TDD-with-no-story-1"), new Tdd("text", null))),
                new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),
                        null, true,
                        Map.of(new TddId("TDD-with-no-story-2"), new Tdd("text", null)))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forMustHaveStories(new TddId("TDD-with-no-story-1")),
                forMustHaveStories(new TddId("TDD-with-no-story-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_FunctionalRequirementsMustHaveStories() {
        var invalidAu = builderPreFilledWithBlanks().functionalRequirements(Map.of(
                new FunctionalRequirementId("Func-req-with-no-story-1"),
                new FunctionalRequirement("Text", "Source", List.of()),
                new FunctionalRequirementId("Func-req-with-no-story-2"),
                new FunctionalRequirement("Text", "Source", List.of())))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forMustHaveStories(new FunctionalRequirementId("Func-req-with-no-story-1")),
                forMustHaveStories(new FunctionalRequirementId("Func-req-with-no-story-2")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_LinksAreAvailable() {
        var invalidAu = builderPreFilledWithBlanks()
                .p1(new P1("n/a", new Jira("ticket", "n/a"), "exec summary"))
                .p2(new P2(null, new Jira("ticket", null)))
                .usefulLinks(List.of(new Link("desc", null)))
                .milestoneDependencies(List.of(new MilestoneDependency("milestone desc", List.of(new Link("desc", "n/a")))))
                .capabilityContainer(new CapabilitiesContainer(
                        Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("N/A").build()).build(),
                        List.of(new FeatureStory("Title", new Jira("ticket", "n/a"), List.of(), List.of()))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forNotAvailableLink("P1.link"),
                forNotAvailableLink("P1.jira.link"),
                forNotAvailableLink("P2.link"),
                forNotAvailableLink("P2.jira.link"),
                forNotAvailableLink("capabilities.epic.jira.link"),
                forNotAvailableLink("capabilities.featurestory.jira.ticket ticket link"),
                forNotAvailableLink("Useful link desc link"),
                forNotAvailableLink("Milestone dependency milestone desc link"));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldAllowJiraLinksToBeEmpty() {
        var invalidAu = builderPreFilledWithBlanks()
                .p1(new P1("validLink", new Jira("ticket", "validLink"), "exec summary"))
                .p2(new P2("validLink", new Jira("ticket", "validLink")))
                .usefulLinks(List.of(new Link("desc", "validLink")))
                .milestoneDependencies(List.of(new MilestoneDependency("milestone desc", List.of(new Link("desc", "validLink")))))
                .capabilityContainer(new CapabilitiesContainer(
                        Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("validLink").build()).build(),
                        singletonList(new FeatureStory("Title", new Jira("jira ticket", null), List.of(), List.of()))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors, not(hasItem(forNotAvailableLink("capabilities.featurestory.jira.ticket jira ticket link"))));
    }

    @Test
    public void shouldValidate_ComponentIdIsMatchingPath() {
        var tdds = new HashMap<TddId, Tdd>();
        tdds.put(new TddId("1"), new Tdd("abc", null));
        var invalidAu = builderPreFilledWithBlanks()
                .p1(new P1("valid", new Jira("ticket", "valid"), "exec summary"))
                .p2(new P2("valid", new Jira("ticket", "valid")))
                .usefulLinks(List.of(new Link("desc", "valid")))
                .milestoneDependencies(List.of(new MilestoneDependency("milestone desc", List.of(new Link("desc", "valid")))))
                .tddContainersByComponent(List.of(
                        new TddContainerByComponent(new TddComponentReference("14"), "bad path", false, tdds),
                        new TddContainerByComponent(new TddComponentReference("15"), null, false, tdds),
                        new TddContainerByComponent(new TddComponentReference("16"), "bad path on deleted component", false, tdds)))
                .capabilityContainer(new CapabilitiesContainer(
                        Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("valid").build()).build(),
                        singletonList(new FeatureStory("Title", new Jira("ticket", "valid"), List.of(new TddId("1")), List.of()))))
                .build();

        var actualErrors = validate(invalidAu, hasMissingComponentDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forComponentPathNotMatchingId("14"),
                forComponentPathNotMatchingId("16"));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_OnlyOneTddContentsReference() {
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(
                new TddContainerByComponent(
                        new TddComponentReference("10"),
                        null,
                        false,
                        Map.of(
                                new TddId("TDD 1.1"), new Tdd("text", "file"),
                                new TddId("TDD OK file"), new Tdd(null, "file"),
                                new TddId("TDD OK text"), new Tdd("text", null)))))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors =
                singletonList(forAmbiguousTddContentReference(new TddComponentReference("10"), new TddId("TDD 1.1")));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));

        collector.checkThat(actualErrors, not(hasItem(forAmbiguousTddContentReference(new TddComponentReference("10"), new TddId("TDD OK file")))));
        collector.checkThat(actualErrors, not(hasItem(forAmbiguousTddContentReference(new TddComponentReference("10"), new TddId("TDD OK text")))));
    }

    @Test
    public void shouldValidate_TddContentsFileExists() {
        String errorFilename1 = "TDD 1.1 : Component-10.md";
        String errorFilename2 = "TDD 1.2 : Component-10.md";
        String noErrorFilename = "TDD 2.1 : Component-10.md";
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(singletonList(
                new TddContainerByComponent(
                        new TddComponentReference("10"),
                        null, false,
                        Map.of(
                                new TddId("TDD 1.1"), new Tdd("overridden-text", null),
                                new TddId("TDD 1.2"), new Tdd("", null),
                                new TddId("TDD 2.1"), new Tdd(null, noErrorFilename)))))
                .tddContents(List.of(
                        new TddContent("contents", errorFilename1),
                        new TddContent("contents", errorFilename2),
                        new TddContent("contents", noErrorFilename),
                        new TddContent("contents", "UNRELATED-DECISION : Component-0.md")))
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forOverriddenByTddContentFile(new TddComponentReference("10"), new TddId("TDD 1.1"), errorFilename1),
                forOverriddenByTddContentFile(new TddComponentReference("10"), new TddId("TDD 1.2"), errorFilename2));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
        collector.checkThat(actualErrors, not(hasItem(forOverriddenByTddContentFile(new TddComponentReference("10"), new TddId("TDD 2.1"), noErrorFilename))));
    }

    @Test
    public void shouldValidate_getErrors_TddsMustHaveOnlyOneTddContentFile() {
        var tddContents = List.of(
                new TddContent("contents", "TDD 1.1 : Component-10.md"),
                new TddContent("contents", "TDD 1.1 : Component-10.txt"));
        var invalidAu = builderPreFilledWithBlanks().tddContainersByComponent(List.of(new TddContainerByComponent(
                        new TddComponentReference("10"),
                        null, false,
                        Map.of(new TddId("TDD 1.1"), new Tdd(null, null)))))
                .tddContents(tddContents)
                .build();

        var actualErrors = validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors =
                singletonList(forMultipleTddContentFilesForTdd(new TddComponentReference("10"), new TddId("TDD 1.1"), tddContents));

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }
}
