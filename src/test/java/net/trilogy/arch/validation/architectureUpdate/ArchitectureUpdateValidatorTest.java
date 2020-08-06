package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE;
import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_BEFORE_UPDATE;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.*;
import static org.hamcrest.Matchers.*;

public class ArchitectureUpdateValidatorTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private ArchitectureDataStructure validDataStructure;
    private ArchitectureDataStructure hasMissingComponentDataStructure;

    @Before
    public void setUp() throws IOException {
        final String ValidArchAsString = new FilesFacade().readString(new File(
                getClass().getResource(MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE).getPath()
        ).toPath());
        validDataStructure = new ArchitectureDataStructureObjectMapper().readValue(ValidArchAsString);

        final String missingComponentArchAsString = new FilesFacade().readString(new File(
                getClass().getResource(MANIFEST_PATH_TO_TEST_AU_VALIDATION_BEFORE_UPDATE).getPath()
        ).toPath());
        hasMissingComponentDataStructure = new ArchitectureDataStructureObjectMapper().readValue(missingComponentArchAsString);
    }

    @Test
    public void blankAuShouldBeValid() {
        var result = ArchitectureUpdateValidator.validate(ArchitectureUpdate.blank(), validDataStructure, validDataStructure);

        collector.checkThat(result.isValid(), is(true));
        collector.checkThat(result.isValid(ValidationStage.STORY), is(true));
        collector.checkThat(result.isValid(ValidationStage.TDD), is(true));
    }

    @Test
    public void shouldValidate_DecisionsMustHaveTdds() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .decisions(
                        Map.of(
                                new Decision.Id("Null TDD references"), new Decision("[SAMPLE DECISION TEXT]", null),
                                new Decision.Id("Empty TDD references"), new Decision("Decision Text", List.of())
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forDecisionsMustHaveTdds(new Decision.Id("Null TDD references")),
                forDecisionsMustHaveTdds(new Decision.Id("Empty TDD references"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_DecisionsTddsMustBeValidReferences() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .decisions(
                        Map.of(
                                new Decision.Id("Bad-TDD-Decision"), new Decision("Decision Text", List.of(new Tdd.Id("BAD-TDD-ID")))
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        collector.checkThat(actualErrors, hasItem(
                forTddsMustBeValidReferences(new Decision.Id("Bad-TDD-Decision"), new Tdd.Id("BAD-TDD-ID"))
        ));
    }

    @Test
    public void shouldValidate_FunctionalRequirementsTddsMustBeValidReferences() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .functionalRequirements(
                        Map.of(
                                new FunctionalRequirement.Id("Bad-TDD-Functional-Requirement"),
                                new FunctionalRequirement("Text", "Source", List.of(
                                        new Tdd.Id("BAD-TDD-ID-1"),
                                        new Tdd.Id("BAD-TDD-ID-2")
                                ))
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsMustBeValidReferences(new FunctionalRequirement.Id("Bad-TDD-Functional-Requirement"), new Tdd.Id("BAD-TDD-ID-1")),
                forTddsMustBeValidReferences(new FunctionalRequirement.Id("Bad-TDD-Functional-Requirement"), new Tdd.Id("BAD-TDD-ID-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsMustHaveUniqueIds() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        true,
                                        Map.of(new Tdd.Id("Dupe-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        true,
                                        Map.of(new Tdd.Id("Dupe-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        true,
                                        Map.of(new Tdd.Id("Dupe-2"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        true,
                                        Map.of(new Tdd.Id("Dupe-2"), new Tdd("text", null))
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forDuplicatedTdd(new Tdd.Id("Dupe-1")),
                forDuplicatedTdd(new Tdd.Id("Dupe-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_ComponentsMustBeReferencedOnlyOnceForTdds() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-1"),
                                        false,
                                        Map.of(new Tdd.Id("1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-1"),
                                        false,
                                        Map.of(new Tdd.Id("2"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-2"),
                                        false,
                                        Map.of(new Tdd.Id("3"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-2"),
                                        false,
                                        Map.of(new Tdd.Id("4"), new Tdd("text", null))
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forDuplicatedComponent(new Tdd.ComponentReference("Dupe-1")),
                forDuplicatedComponent(new Tdd.ComponentReference("Dupe-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsComponentsMustBeValidReferences() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Non-existent-1"),
                                        false,
                                        Map.of(new Tdd.Id("1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Non-existent-2"),
                                        false,
                                        Map.of(new Tdd.Id("2"), new Tdd("text", null))
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Non-existent-1")),
                forTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Non-existent-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }


    @Test
    public void shouldValidate_TddsDeletedComponentsMustBeValidReferences() {
        // Given
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Non-existent-deleted-1"),
                                        true,
                                        Map.of(new Tdd.Id("1"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Non-existent-deleted-2"),
                                        true,
                                        Map.of(new Tdd.Id("2"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Valid-Deleted-Component-Id"),  // Present in beforeUpdate Architecture
                                        true,
                                        Map.of(new Tdd.Id("3"), new Tdd("text", null))
                                )
                        )
                ).build();

        // When
        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, hasMissingComponentDataStructure).getErrors();

        // Then
        var expectedErrors = List.of(
                forDeletedTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Non-existent-deleted-1")),
                forDeletedTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Non-existent-deleted-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));

        // And ensure that the valid deleted component is not present in errors
        collector.checkThat(actualErrors,
                not(hasItem(forDeletedTddsComponentsMustBeValidReferences(
                        new Tdd.ComponentReference("Valid-Deleted-Component-Id")
                )))
        );
    }

    @Test
    public void shouldValidate_TddsMustHaveDecisionsOrRequirements() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("[SAMPLE-COMPONENT-ID]"),
                                        false,
                                        Map.of(new Tdd.Id("No-decision-or-req-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("[SAMPLE-COMPONENT-ID]"),
                                        true,
                                        Map.of(new Tdd.Id("No-decision-or-req-2"), new Tdd("text", null))
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsMustHaveDecisionsOrRequirements(new Tdd.Id("No-decision-or-req-1")),
                forTddsMustHaveDecisionsOrRequirements(new Tdd.Id("No-decision-or-req-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_StoriesMustHaveFunctionalRequirements() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(Tdd.Id.blank()),
                                                List.of()
                                        ),
                                        new FeatureStory("Feat Title 2", Jira.blank(), List.of(Tdd.Id.blank()),
                                                null
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forStoriesMustHaveFunctionalRequirements("Feat Title 1"),
                forStoriesMustHaveFunctionalRequirements("Feat Title 2")
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValiate_StoriesFunctionalRequirementsMustBeValidReferences() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(Tdd.Id.blank()),
                                                List.of(new FunctionalRequirement.Id("Invalid-Functional-Requirement"))
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forFunctionalRequirementsMustBeValidReferences("Feat Title 1", new FunctionalRequirement.Id("Invalid-Functional-Requirement"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_StoriesMustHaveTdds() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(),
                                                List.of(), // Empty TDD reference
                                                List.of()
                                        ), new FeatureStory(
                                                "Feat Title 2", Jira.blank(),
                                                null, // Null TDD reference
                                                List.of()
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forStoriesMustHaveTdds("Feat Title 1"),
                forStoriesMustHaveTdds("Feat Title 2")
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_StoriesTddsMustBeValidReferences() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(),
                                                List.of(new Tdd.Id("Invalid TDD 1"),
                                                        new Tdd.Id("Invalid TDD 2")),
                                                List.of()
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forStoriesTddsMustBeValidReferences(new Tdd.Id("Invalid TDD 1"), "Feat Title 1"),
                forStoriesTddsMustBeValidReferences(new Tdd.Id("Invalid TDD 2"), "Feat Title 1")
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_TddsMustHaveStories() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("[SAMPLE-COMPONENT-ID]"),
                                        false,
                                        Map.of(new Tdd.Id("TDD-with-no-story-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("[SAMPLE-COMPONENT-ID]"),
                                        true,
                                        Map.of(new Tdd.Id("TDD-with-no-story-2"), new Tdd("text", null))
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forMustHaveStories(new Tdd.Id("TDD-with-no-story-1")),
                forMustHaveStories(new Tdd.Id("TDD-with-no-story-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_FunctionalRequirementsMustHaveStories() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .functionalRequirements(
                        Map.of(
                                new FunctionalRequirement.Id("Func-req-with-no-story-1"),
                                new FunctionalRequirement("Text", "Source", List.of()),
                                new FunctionalRequirement.Id("Func-req-with-no-story-2"),
                                new FunctionalRequirement("Text", "Source", List.of())
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forMustHaveStories(new FunctionalRequirement.Id("Func-req-with-no-story-1")),
                forMustHaveStories(new FunctionalRequirement.Id("Func-req-with-no-story-2"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_LinksAreAvailable() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .p1(new P1("n/a", new Jira("ticket", "n/a"), "exec summary"))
                .p2(new P2(null, new Jira("ticket", null)))
                .usefulLinks(List.of(new Link("desc", null)))
                .milestoneDependencies(List.of(new MilestoneDependency("milestone desc", List.of(new Link("desc", "n/a")))))
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("N/A").build()).build(),
                                List.of(
                                        new FeatureStory("Title", new Jira("ticket", "n/a"), List.of(), List.of())
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forNotAvailableLink("P1.link"),
                forNotAvailableLink("P1.jira.link"),
                forNotAvailableLink("P2.link"),
                forNotAvailableLink("P2.jira.link"),
                forNotAvailableLink("capabilities.epic.jira.link"),
                forNotAvailableLink("capabilities.featurestory.jira.ticket ticket link"),
                forNotAvailableLink("Useful link desc link"),
                forNotAvailableLink("Milestone dependency milestone desc link")
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }
}
