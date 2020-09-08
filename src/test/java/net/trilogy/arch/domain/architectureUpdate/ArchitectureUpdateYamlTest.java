package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.trilogy.arch.domain.architectureUpdate.Decision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_MAPPER;
import static org.junit.Assert.assertEquals;

public class ArchitectureUpdateYamlTest {
    // Do not configure for YAML: round-tripping as JSON is enough. We're not
    // testing the Jackson library on JSON vs YAML serialization
    public static final ObjectMapper mapper = YAML_MAPPER;

    @Ignore("TODO: TDD contents get lost on round trip")
    @Test
    public void round_trips_to_and_from_yaml() throws JsonProcessingException {
        var au = new ArchitectureUpdate(
                "some AU name",
                "some AU milestone",
                singletonList(new Person(
                        "some AUTHOR PERSON name",
                        "some AUTHOR PERSON email")),
                singletonList(new Person(
                        "some PCA PERSON name",
                        "some PCA PERSON email")),
                singletonMap(
                        new DecisionId("some DECISION id"),
                        new Decision(
                                "some DECISION text",
                                singletonList(new TddId("TODO: is this not the same decision id?")))),
                singletonList(new TddContainerByComponent(
                        new TddComponentReference("some TDD component reference"),
                        "some TDD container-by-component path",
                        false, // TODO: Super smell
                        singletonMap(
                                new TddId("TODO: How is this different from the reference?"),
                                new Tdd(
                                        "some TDD text",
                                        "some TDD file")))),
                singletonMap(
                        new FunctionalRequirementId("some FUNC REQ id"),
                        new FunctionalRequirement(
                                "some FUNC REQ text",
                                "some FUNC REQ source",
                                singletonList(new TddId("some func req TDD id"))
                        )),
                new CapabilitiesContainer(
                        new Epic(
                                "some EPIC title",
                                new Jira(
                                        "some epic JIRA ticket",
                                        "some epic JIRA link")),
                        singletonList(new FeatureStory(
                                "some FEATURE STORY title",
                                new Jira(
                                        "some feature story JIRA ticket",
                                        "some feature story JIRA link"),
                                singletonList(new TddId("some feature story TDD id")),
                                singletonList(new FunctionalRequirementId("some feature story func req id"))))),
                new P2(
                        "some link to P2",
                        new Jira(
                                "some P2 JIRA ticket",
                                "some P2 JIRA link")),
                new P1(
                        "some link to P1",
                        new Jira(
                                "some P1 JIRA ticket",
                                "some P1 JIRA link"),
                        "some P1 executive summary"),
                singletonList(new Link(
                        "some USEFUL LINK description",
                        "some USEFUL LINK link"
                )),
                singletonList(new MilestoneDependency(
                        "some MILESTONE description",
                        singletonList(new Link(
                                "some MILESTONE DEPENDENCY description",
                                "some MILESTONE DEPENDENCY link"
                        )))),
                singletonList(new TddContent(
                        "some TDD content",
                        "some TDD filename")));

        assertEquals(
                au,
                mapper.readValue(mapper.writeValueAsString(au), ArchitectureUpdate.class)
        );
    }
}
