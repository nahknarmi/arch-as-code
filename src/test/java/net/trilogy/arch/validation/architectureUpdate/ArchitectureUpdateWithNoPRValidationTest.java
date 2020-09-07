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
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateWithNoPRValidationTest {
    private ArchitectureDataStructure validDataStructure;

    @Before
    public void setUp() throws IOException {
        final String ValidArchAsString = new FilesFacade().readString(new File(
                getClass().getResource(MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE).getPath()
        ).toPath());
        validDataStructure = new ArchitectureDataStructureObjectMapper().readValue(ValidArchAsString);
    }

    // Decisions
    @Test
    public void shouldAllowMultipleDecisionsWithNoPR() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .decisions(
                        Map.of(
                                new DecisionId("first Decision"), new Decision("[SAMPLE DECISION TEXT]", List.of(new TddId("no-PR"))),
                                new DecisionId("second Decision"), new Decision("Decision Text", List.of(new TddId("no-PR")))
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldAllowADecisionsWithNoPR() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .decisions(
                        Map.of(
                                new DecisionId("first Decision"), new Decision("[SAMPLE DECISION TEXT]", List.of(new TddId("no-PR")))
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldNotAllowADecisionsWithNoPRandATDDReference() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .decisions(
                        Map.of(
                                new DecisionId("first Decision"), new Decision("[SAMPLE DECISION TEXT]", List.of(new TddId("no-PR"), TddId.blank()))
                        )
                )
                .tddContainersByComponent(List.of(new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),  // Present in beforeUpdate Architecture
                        null, false,
                        Map.of(TddId.blank(), new Tdd("text", null))
                )))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(1));
        assertThat(actualErrors.get(0).getDescription(), equalTo("Decision [SAMPLE DECISION TEXT] has no-PR, and shouldn't be combined with another TDD"));
    }

    // Feature Stories
    @Test
    public void shouldAllowMultipleStoriesWithNoPR() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(TddId.noPr()),
                                                List.of(FunctionalRequirementId.blank())
                                        ),
                                        new FeatureStory("Feat Title 2", Jira.blank(), List.of(TddId.noPr()),
                                                List.of(FunctionalRequirementId.blank())
                                        ),
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(TddId.blank()),
                                                List.of(FunctionalRequirementId.blank())
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldAllowAStoryWithNoPR() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(TddId.noPr()),
                                                List.of(FunctionalRequirementId.blank())
                                        ),
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(TddId.blank()),
                                                List.of(FunctionalRequirementId.blank())
                                        )
                                )
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldNotAllowAStoryWithNoPRandATDDReference() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        new CapabilitiesContainer(
                                Epic.blank(),
                                List.of(
                                        new FeatureStory(
                                                "Feat Title 1", Jira.blank(), List.of(TddId.noPr(), TddId.blank()),
                                                List.of(FunctionalRequirementId.blank())
                                        ),
                                        new FeatureStory("Feat Title 2", Jira.blank(), List.of(TddId.blank()),
                                                List.of(FunctionalRequirementId.blank())
                                        )
                                )
                        ))
                .tddContainersByComponent(List.of(new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),  // Present in beforeUpdate Architecture
                        null, false,
                        Map.of(TddId.blank(), new Tdd("text", null))
                )))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(1));
        assertThat(actualErrors.get(0).getDescription(), equalTo("Feature story Feat Title 1 has no-PR, and shouldn't be combined with another TDD"));
    }

    // Functional Requirements
    @Test
    public void shouldAllowMultipleFunctionalRequirementsWithNoPR() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .functionalRequirements(
                        Map.of(
                                FunctionalRequirementId.blank(),
                                new FunctionalRequirement("Text", "Source", List.of(TddId.noPr()))
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldAllowAFunctionalRequirementWithNoPR() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .functionalRequirements(
                        Map.of(
                                FunctionalRequirementId.blank(),
                                new FunctionalRequirement("Text", "Source", List.of(TddId.noPr()))
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldNotAllowAFunctionalRequirementWithNoPRandATDDReference() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .functionalRequirements(
                        Map.of(
                                FunctionalRequirementId.blank(),
                                new FunctionalRequirement("Text", "Source", List.of(TddId.noPr(), TddId.blank()))
                        ))
                .tddContainersByComponent(List.of(new TddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),  // Present in beforeUpdate Architecture
                        null, false,
                        Map.of(TddId.blank(), new Tdd("text", null))
                )))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(1));
        assertThat(actualErrors.get(0).getDescription(), equalTo("Functional requirement Text has no-PR, and shouldn't be combined with another TDD"));
    }

    @Test
    public void shouldAllowAFunctionalRequirementWithNullTdds() {
        ArchitectureUpdate invalidAu = ArchitectureUpdate.builderPreFilledWithBlanks()
                .functionalRequirements(
                        Map.of(
                                FunctionalRequirementId.blank(),
                                new FunctionalRequirement("Text", "Source", null)
                        )
                ).build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validDataStructure, validDataStructure).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }
}
