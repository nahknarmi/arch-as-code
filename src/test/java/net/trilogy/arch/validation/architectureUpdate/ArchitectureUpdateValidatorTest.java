package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.CapabilitiesContainer;
import net.trilogy.arch.domain.architectureUpdate.Decision;
import net.trilogy.arch.domain.architectureUpdate.Epic;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.domain.architectureUpdate.Link;
import net.trilogy.arch.domain.architectureUpdate.MilestoneDependency;
import net.trilogy.arch.domain.architectureUpdate.P1;
import net.trilogy.arch.domain.architectureUpdate.P2;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
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

import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE;
import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_BEFORE_UPDATE;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forAmbiguousTddContentReference;
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static net.trilogy.arch.validation.architectureUpdate.ValidationError.*;
import static org.hamcrest.MatcherAssert.assertThat;
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
                                        null, true,
                                        Map.of(new Tdd.Id("Dupe-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        null, true,
                                        Map.of(new Tdd.Id("Dupe-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        null, true,
                                        Map.of(new Tdd.Id("Dupe-2"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("1"),
                                        null, true,
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
                                        null, false,
                                        Map.of(new Tdd.Id("1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-1"),
                                        null, false,
                                        Map.of(new Tdd.Id("2"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-2"),
                                        null, false,
                                        Map.of(new Tdd.Id("3"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Dupe-2"),
                                        null, false,
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
                                        null, false,
                                        Map.of(new Tdd.Id("1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("Non-existent-2"),
                                        null, false,
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
                                        null, true,
                                        Map.of(new Tdd.Id("1"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Non-existent-deleted-2"),
                                        null, true,
                                        Map.of(new Tdd.Id("2"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Valid-Deleted-Component-Id"),  // Present in beforeUpdate Architecture
                                        null, true,
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
                                        null, false,
                                        Map.of(new Tdd.Id("No-decision-or-req-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("[SAMPLE-COMPONENT-ID]"),
                                        null, true,
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
                                        null, false,
                                        Map.of(new Tdd.Id("TDD-with-no-story-1"), new Tdd("text", null))
                                ), new TddContainerByComponent(
                                        new Tdd.ComponentReference("[SAMPLE-COMPONENT-ID]"),
                                        null, true,
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

    @Test
    public void shouldAllowJiraLinksToBeEmpty() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .p1(new P1("validLink", new Jira("ticket", "validLink"), "exec summary"))
                .p2(new P2("validLink", new Jira("ticket", "validLink")))
                .usefulLinks(List.of(new Link("desc", "validLink")))
                .milestoneDependencies(List.of(new MilestoneDependency("milestone desc", List.of(new Link("desc", "validLink")))))
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("validLink").build()).build(),
                                List.of(
                                        new FeatureStory("Title", new Jira("jira ticket", null), List.of(), List.of())
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors, not(hasItem(forNotAvailableLink("capabilities.featurestory.jira.ticket jira ticket link"))));
    }

    @Test
    public void shouldValidate_ComponentIdIsMatchingPath() {
        var tdds = new HashMap<Tdd.Id, Tdd>();
        tdds.put(new Tdd.Id("1"), new Tdd("abc", null));
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .p1(new P1("valid", new Jira("ticket", "valid"), "exec summary"))
                .p2(new P2("valid", new Jira("ticket", "valid")))
                .usefulLinks(List.of(new Link("desc", "valid")))
                .milestoneDependencies(List.of(new MilestoneDependency("milestone desc", List.of(new Link("desc", "valid")))))
                .tddContainersByComponent(List.of(
                        new TddContainerByComponent(new Tdd.ComponentReference("14"), "bad path", false, tdds),
                        new TddContainerByComponent(new Tdd.ComponentReference("15"), null, false, tdds),
                        new TddContainerByComponent(new Tdd.ComponentReference("16"), "bad path on deleted component", false, tdds)
                        ))
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("valid").build()).build(),
                                List.of(
                                        new FeatureStory("Title", new Jira("ticket", "valid"), List.of(new Tdd.Id("1")), List.of())
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, hasMissingComponentDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forComponentPathNotMatchingId("14"),
                forComponentPathNotMatchingId("16")
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }

    @Test
    public void shouldValidate_OnlyOneTddContentsReference() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("10"),
                                        null, false,
                                        Map.of(
                                                new Tdd.Id("TDD 1.1"), new Tdd("text", "file"),
                                                new Tdd.Id("TDD OK file"), new Tdd(null, "file"),
                                                new Tdd.Id("TDD OK text"), new Tdd("text", null)
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forAmbiguousTddContentReference(new Tdd.ComponentReference("10"), new Tdd.Id("TDD 1.1"))
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));

        collector.checkThat(actualErrors, not(hasItem(forAmbiguousTddContentReference(new Tdd.ComponentReference("10"), new Tdd.Id("TDD OK file")))));
        collector.checkThat(actualErrors, not(hasItem(forAmbiguousTddContentReference(new Tdd.ComponentReference("10"), new Tdd.Id("TDD OK text")))));
    }

    @Test
    public void shouldValidate_TddContentsFileExists() {
        String errorFilename1 = "TDD 1.1 : Component-10.md";
        String errorFilename2 = "TDD 1.2 : Component-10.md";
        String noErrorFilename = "TDD 2.1 : Component-10.md";
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("10"),
                                        null, false,
                                        Map.of(
                                                new Tdd.Id("TDD 1.1"), new Tdd("overridden-text", null),
                                                new Tdd.Id("TDD 1.2"), new Tdd("", null),
                                                new Tdd.Id("TDD 2.1"), new Tdd(null, noErrorFilename))
                                )
                        )
                ).tddContents(List.of(
                        new TddContent("contents", errorFilename1),
                        new TddContent("contents", errorFilename2),
                        new TddContent("contents", noErrorFilename),
                        new TddContent("contents", "UNRELATED-DECISION : Component-0.md")
                ))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forOverriddenByTddContentFile(new Tdd.ComponentReference("10"), new Tdd.Id("TDD 1.1"), errorFilename1),
                forOverriddenByTddContentFile(new Tdd.ComponentReference("10"), new Tdd.Id("TDD 1.2"), errorFilename2)
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
        collector.checkThat(actualErrors, not(hasItem(forOverriddenByTddContentFile(new Tdd.ComponentReference("10"), new Tdd.Id("TDD 2.1"), noErrorFilename))));
    }

    @Test
    public void shouldValidate_getErrors_TddsMustHaveOnlyOneTddContentFile() {
        List<TddContent> tddContents = List.of(
                new TddContent("contents", "TDD 1.1 : Component-10.md"),
                new TddContent("contents", "TDD 1.1 : Component-10.txt")
        );
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .tddContainersByComponent(
                        List.of(new TddContainerByComponent(
                                new Tdd.ComponentReference("10"),
                                null, false,
                                Map.of(new Tdd.Id("TDD 1.1"), new Tdd(null, null)))
                        )
                ).tddContents(tddContents)
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        var expectedErrors = List.of(
                forMultipleTddContentFilesForTdd(new Tdd.ComponentReference("10"), new Tdd.Id("TDD 1.1"), tddContents)
        );

        expectedErrors.forEach(e -> collector.checkThat(actualErrors, hasItem(e)));
    }
}
