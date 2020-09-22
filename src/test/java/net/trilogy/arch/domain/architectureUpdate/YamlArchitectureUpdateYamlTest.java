package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.trilogy.arch.domain.architectureUpdate.YamlDecision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static org.junit.Assert.assertEquals;

public class YamlArchitectureUpdateYamlTest {
    @Ignore("TODO: TDD contents get lost on round trip")
    @Test
    public void round_trips_to_and_from_yaml() throws JsonProcessingException {
        var au = new YamlArchitectureUpdate(
                "some AU name",
                "some AU milestone",
                singletonList(new YamlPerson(
                        "some AUTHOR PERSON name",
                        "some AUTHOR PERSON email")),
                singletonList(new YamlPerson(
                        "some PCA PERSON name",
                        "some PCA PERSON email")),
                singletonMap(
                        new DecisionId("some DECISION id"),
                        new YamlDecision(
                                "some DECISION text",
                                singletonList(new TddId("TODO: is this not the same decision id?")))),
                singletonList(new YamlTddContainerByComponent(
                        new TddComponentReference("some TDD component reference"),
                        "some TDD container-by-component path",
                        false, // TODO: Super smell
                        singletonMap(
                                new TddId("TODO: How is this different from the reference?"),
                                new YamlTdd(
                                        "some TDD text",
                                        "some TDD file")))),
                singletonMap(
                        new FunctionalRequirementId("some FUNC REQ id"),
                        new YamlFunctionalRequirement(
                                "some FUNC REQ text",
                                "some FUNC REQ source",
                                singletonList(new TddId("some func req TDD id"))
                        )),
                singletonMap(YamlFunctionalArea.FunctionalAreaId.blank(), YamlFunctionalArea.blank()),
                new YamlCapabilitiesContainer(
                        new YamlEpic(
                                "some EPIC title",
                                new YamlJira(
                                        "some epic JIRA ticket",
                                        "some epic JIRA link")),
                        singletonList(new YamlFeatureStory(
                                "some FEATURE STORY title",
                                new YamlJira(
                                        "some feature story JIRA ticket",
                                        "some feature story JIRA link"),
                                singletonList(new TddId("some feature story TDD id")),
                                singletonList(new FunctionalRequirementId("some feature story func req id")),
                                YamlE2E.blank()))),
                new YamlP2(
                        "some link to P2",
                        new YamlJira(
                                "some P2 JIRA ticket",
                                "some P2 JIRA link")),
                new YamlP1(
                        "some link to P1",
                        new YamlJira(
                                "some P1 JIRA ticket",
                                "some P1 JIRA link"),
                        "some P1 executive summary"),
                singletonList(new YamlLink(
                        "some USEFUL LINK description",
                        "some USEFUL LINK link"
                )),
                singletonList(new YamlMilestoneDependency(
                        "some MILESTONE description",
                        singletonList(new YamlLink(
                                "some MILESTONE DEPENDENCY description",
                                "some MILESTONE DEPENDENCY link"
                        ))))
        );

        assertEquals(
                au,
                YAML_OBJECT_MAPPER.readValue(YAML_OBJECT_MAPPER.writeValueAsString(au), YamlArchitectureUpdate.class)
        );
    }
}
