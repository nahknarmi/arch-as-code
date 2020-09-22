package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.domain.architectureUpdate.YamlDecision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE;
import static net.trilogy.arch.Util.first;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate.prefilledYamlArchitectureUpdateWithBlanks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateWithNoPRValidationTest {
    private ArchitectureDataStructure validADS;

    @Before
    public void setUp() throws IOException {
        final var ValidArchAsString = new FilesFacade().readString(new File(
                getClass().getResource(MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE).getPath())
                .toPath());

        validADS = YAML_OBJECT_MAPPER.readValue(ValidArchAsString, ArchitectureDataStructure.class);
    }

    // Decisions
    @Test
    public void shouldAllowMultipleDecisionsWithNoPR() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .decisions(Map.of(
                        new DecisionId("first Decision"), new YamlDecision("[SAMPLE DECISION TEXT]", List.of(new TddId("no-PR"))),
                        new DecisionId("second Decision"), new YamlDecision("Decision Text", List.of(new TddId("no-PR")))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldAllowADecisionsWithNoPR() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .decisions(Map.of(
                        new DecisionId("first Decision"), new YamlDecision("[SAMPLE DECISION TEXT]", List.of(new TddId("no-PR")))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldNotAllowADecisionsWithNoPRandATDDReference() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .decisions(Map.of(
                        new DecisionId("first Decision"), new YamlDecision("[SAMPLE DECISION TEXT]", List.of(new TddId("no-PR"), TddId.blank()))))
                .tddContainersByComponent(List.of(new YamlTddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),  // Present in beforeUpdate Architecture
                        null, false,
                        Map.of(TddId.blank(), new YamlTdd("text", null)))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(1));
        assertThat(first(actualErrors).getDescription(), equalTo("Decision [SAMPLE DECISION TEXT] has no-PR, and shouldn't be combined with another TDD"));
    }

    // Feature Stories
    @Test
    public void shouldAllowMultipleStoriesWithNoPR() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .capabilityContainer(new YamlCapabilitiesContainer(
                        YamlEpic.blank(),
                        List.of(
                                new YamlFeatureStory(
                                        "Feat Title 1", YamlJira.blank(), List.of(TddId.noPr()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()),
                                new YamlFeatureStory("Feat Title 2", YamlJira.blank(), List.of(TddId.noPr()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()),
                                new YamlFeatureStory(
                                        "Feat Title 1", YamlJira.blank(), List.of(TddId.blank()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldAllowAStoryWithNoPR() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .capabilityContainer(new YamlCapabilitiesContainer(
                        YamlEpic.blank(),
                        List.of(
                                new YamlFeatureStory(
                                        "Feat Title 1", YamlJira.blank(), List.of(TddId.noPr()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()),
                                new YamlFeatureStory(
                                        "Feat Title 1", YamlJira.blank(), List.of(TddId.blank()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldNotAllowAStoryWithNoPRandATDDReference() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .capabilityContainer(new YamlCapabilitiesContainer(
                        YamlEpic.blank(),
                        List.of(
                                new YamlFeatureStory(
                                        "Feat Title 1", YamlJira.blank(), List.of(TddId.noPr(), TddId.blank()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()),
                                new YamlFeatureStory("Feat Title 2", YamlJira.blank(), List.of(TddId.blank()),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()))))
                .tddContainersByComponent(List.of(new YamlTddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),  // Present in beforeUpdate Architecture
                        null, false,
                        Map.of(TddId.blank(), new YamlTdd("text", null)))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(1));
        assertThat(first(actualErrors).getDescription(), equalTo("Feature story Feat Title 1 has no-PR, and shouldn't be combined with another TDD"));
    }

    // Functional Requirements
    @Test
    public void shouldAllowMultipleFunctionalRequirementsWithNoPR() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .functionalRequirements(Map.of(
                        FunctionalRequirementId.blank(),
                        new YamlFunctionalRequirement("Text", "Source", List.of(TddId.noPr()))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldAllowAFunctionalRequirementWithNoPR() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .functionalRequirements(Map.of(
                        FunctionalRequirementId.blank(),
                        new YamlFunctionalRequirement("Text", "Source", List.of(TddId.noPr()))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }

    @Test
    public void shouldNotAllowAFunctionalRequirementWithNoPRandATDDReference() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .functionalRequirements(Map.of(
                        FunctionalRequirementId.blank(),
                        new YamlFunctionalRequirement("Text", "Source", List.of(TddId.noPr(), TddId.blank()))))
                .tddContainersByComponent(List.of(new YamlTddContainerByComponent(
                        new TddComponentReference("[SAMPLE-COMPONENT-ID]"),  // Present in beforeUpdate Architecture
                        null, false,
                        Map.of(TddId.blank(), new YamlTdd("text", null)))))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(1));
        assertThat(first(actualErrors).getDescription(), equalTo("Functional requirement Text has no-PR, and shouldn't be combined with another TDD"));
    }

    @Test
    public void shouldAllowAFunctionalRequirementWithNullTdds() {
        final var invalidAu = prefilledYamlArchitectureUpdateWithBlanks()
                .functionalRequirements(Map.of(
                        FunctionalRequirementId.blank(),
                        new YamlFunctionalRequirement("Text", "Source", null)))
                .build();

        var actualErrors = ArchitectureUpdateValidator.validate(invalidAu, validADS, validADS).getErrors();

        assertThat(actualErrors.size(), equalTo(0));
    }
}
