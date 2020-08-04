package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.adapter.architectureYaml.ArchitectureDataStructureObjectMapper;
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

    private ArchitectureUpdate invalidAu;
    private ArchitectureDataStructure validDataStructure;
    private ArchitectureDataStructure hasMissingComponentDataStructure;

    /*
     * [TODO] [TESTING]: Refactor required.
     *
     * This testing strategy got out of hand. The strategy is:
     *   create an AU with all possible validation errors, and see if the validator
     *   catches them all
     * The recommended strategy is:
     *   make production `getError_` method accessible from test (package private, or extract to its 
     *   own class), and test it individually + have a single test that ensures all errors are aggregated
    */
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

        invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .decisions(Map.of(
                        new Decision.Id("Missing-TDD-Decision-1"), new Decision("Decision-1-text", null),
                        new Decision.Id("Missing-TDD-Decision-2"), new Decision("Decision-2-text", List.of()),
                        new Decision.Id("Bad-TDD-Decision"), new Decision("Decision-2-text", List.of(new Tdd.Id("BAD-TDD-ID"))),
                        new Decision.Id("Valid-Decision"), new Decision("Decision-3-text", List.of(new Tdd.Id("Valid-TDD-with-decision-and-story")))
                ))
                .functionalRequirements(
                        Map.of(
                                new FunctionalRequirement.Id("Bad-TDD-Functional-Requirement"),
                                new FunctionalRequirement("Text", "Source", List.of(
                                        new Tdd.Id("BAD-TDD-ID"),
                                        new Tdd.Id("Tdd-with-invalid-component")
                                )),

                                new FunctionalRequirement.Id("Valid-Functional-Requirement"),
                                new FunctionalRequirement("Text", "Source", List.of()),

                                new FunctionalRequirement.Id("Functional-Requirement-Without-Story"),
                                new FunctionalRequirement("Text", "Source", List.of(
                                        new Tdd.Id("Valid-TDD-with-requirement-and-story"),
                                        new Tdd.Id("Valid-TDD-with-requirement-and-story-2"),
                                        new Tdd.Id("Valid-TDD-with-requirement-and-story-3"),
                                        new Tdd.Id("Valid-TDD-with-requirement-and-story-4"),
                                        new Tdd.Id("Valid-TDD-with-requirement-and-story-5")
                                ))
                        )
                )
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Valid-Deleted-Component-Id"),
                                        true,
                                        Map.of(new Tdd.Id("Valid-TDD-with-requirement-and-story-2"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("38"),
                                        false,
                                        Map.of(new Tdd.Id("Valid-TDD-with-requirement-and-story"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("14"),
                                        false,
                                        Map.of(new Tdd.Id("TDD-unused-and-without-story"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("14"),
                                        true, // same id as a non-deleted one-- they should not clash
                                        Map.of(new Tdd.Id("Valid-TDD-with-requirement-and-story-5"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("15"),
                                        false,
                                        Map.of(
                                                new Tdd.Id("Valid-TDD-with-decision-and-story"), new Tdd("text", null),
                                                new Tdd.Id("TDD-unused-with-story"), new Tdd("text", null)
                                        )
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Invalid-Component-Id"),
                                        false,
                                        Map.of(
                                                new Tdd.Id("Tdd-with-invalid-component"), new Tdd("text", null),
                                                new Tdd.Id("Valid-TDD-with-requirement-and-story"), new Tdd("INVALID BECAUSE DUPLICATED ID", null)
                                        )
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Invalid-Component-Id-2"),
                                        null,
                                        Map.of(new Tdd.Id("Valid-TDD-with-requirement-and-story-3"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("Invalid-Deleted-Component-Id"),
                                        true,  // ERR: Because this component never existed
                                        Map.of(new Tdd.Id("Valid-TDD-with-requirement-and-story-4"), new Tdd("text", null))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("38"), // ERR: duplicated
                                        false,
                                        Map.of()
                                )
                        )
                )
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.builder().title("epic").jira(Jira.builder().ticket("ticket").link("N/A").build()).build(),
                                List.of(
                                        new FeatureStory("Feat Title", Jira.blank(), List.of(
                                                new Tdd.Id("Valid-TDD-with-requirement-and-story"),
                                                new Tdd.Id("Valid-TDD-with-requirement-and-story-2"),
                                                new Tdd.Id("Valid-TDD-with-requirement-and-story-3"),
                                                new Tdd.Id("Valid-TDD-with-requirement-and-story-4"),
                                                new Tdd.Id("Valid-TDD-with-requirement-and-story-5"),
                                                new Tdd.Id("Invalid-TDD-ID"),
                                                new Tdd.Id("Tdd-with-invalid-component")
                                        ), List.of(
                                                new FunctionalRequirement.Id("Valid-Functional-Requirement")
                                        )),

                                        new FeatureStory("Feat Title 2", Jira.blank(), List.of(
                                                new Tdd.Id("Valid-TDD-with-decision-and-story"),
                                                new Tdd.Id("TDD-unused-with-story")
                                        ), List.of(
                                                new FunctionalRequirement.Id("Bad-TDD-Functional-Requirement"),
                                                new FunctionalRequirement.Id("Bad-Functional-Requirement-ID")
                                        )),

                                        new FeatureStory("Feat Title 3", Jira.blank(), List.of(
                                                new Tdd.Id("Valid-TDD-with-decision-and-story")
                                        ), List.of()),

                                        new FeatureStory("Feat Title 4", Jira.blank(), List.of(), List.of(
                                                new FunctionalRequirement.Id("Valid-Functional-Requirement")
                                        ))
                                )
                        )
                )
                .build();
    }

    @Test
    public void blankAuShouldBeValid() {
        var result = ArchitectureUpdateValidator.validate(ArchitectureUpdate.blank(), validDataStructure, validDataStructure);

        collector.checkThat(result.isValid(), is(true));
        collector.checkThat(result.isValid(ValidationStage.STORY), is(true));
        collector.checkThat(result.isValid(ValidationStage.TDD), is(true));
    }

    @Test
    public void shouldFindAllErrors() {
        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, hasMissingComponentDataStructure).getErrors();

        var expectedErrors = List.of(
                forTddsMustBeValidReferences(new Decision.Id("Bad-TDD-Decision"), new Tdd.Id("BAD-TDD-ID")),
                forTddsMustBeValidReferences(new FunctionalRequirement.Id("Bad-TDD-Functional-Requirement"), new Tdd.Id("BAD-TDD-ID")),

                forDecisionsMustHaveTdds(new Decision.Id("Missing-TDD-Decision-1")),
                forDecisionsMustHaveTdds(new Decision.Id("Missing-TDD-Decision-2")),

                forMustHaveStories(new Tdd.Id("TDD-unused-and-without-story")),
                forMustHaveStories(new FunctionalRequirement.Id("Functional-Requirement-Without-Story")),

                forTddsMustHaveDecisionsOrRequirements(new Tdd.Id("TDD-unused-and-without-story")),
                forTddsMustHaveDecisionsOrRequirements(new Tdd.Id("TDD-unused-with-story")),

                forStoriesTddsMustBeValidReferences(new Tdd.Id("Invalid-TDD-ID"), "Feat Title"),

                forTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Invalid-Component-Id")),
                forTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Invalid-Component-Id-2")),
                forDeletedTddsComponentsMustBeValidReferences(new Tdd.ComponentReference("Invalid-Deleted-Component-Id")),

                forFunctionalRequirementsMustBeValidReferences("Feat Title 2", new FunctionalRequirement.Id("Bad-Functional-Requirement-ID")),

                forStoriesMustHaveTdds("Feat Title 4"),

                forStoriesMustHaveFunctionalRequirements("Feat Title 3"),

                forDuplicatedTdd(new Tdd.Id("Valid-TDD-with-requirement-and-story")),

                forDuplicatedComponent(new Tdd.ComponentReference("38")),

                forNotAvailableLink("capabilities.epic.jira.link")
        );

        expectedErrors.forEach(e ->
                collector.checkThat(actualErrors, hasItem(e)));

        collector.checkThat(actualErrors.size(), equalTo(expectedErrors.size()));
    }
}
