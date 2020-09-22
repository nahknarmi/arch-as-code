package net.trilogy.arch.services.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StoryPublishingServiceTest {
    @Test
    public void shouldListStoriesToBeSentToJira() {
        // GIVEN
        List<YamlFeatureStory> featureStoriesToBeCreated = List.of(
                new YamlFeatureStory("Story Title",
                        new YamlJira("", ""),
                        List.of(new TddId("TDD 1.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ")),
                        YamlE2E.blank()
                ), new YamlFeatureStory("Story Title",
                        new YamlJira(null, null),
                        List.of(new TddId("TDD 2.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ 2")),
                        YamlE2E.blank()
                ), new YamlFeatureStory("Story Title",
                        null,
                        List.of(new TddId("TDD 3.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ 3")),
                        YamlE2E.blank()
                )
        );
        List<YamlFeatureStory> featureStoriesToNotBeCreated = List.of(
                new YamlFeatureStory("Story Exists - Do Not Create",
                        new YamlJira("some ticket", "some link"),
                        List.of(new TddId("TDD 2.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ")),
                        YamlE2E.blank()
                )
        );

        List<YamlFeatureStory> allStories = Stream.concat(featureStoriesToBeCreated.stream(), featureStoriesToNotBeCreated.stream()).collect(Collectors.toList());
        YamlArchitectureUpdate au = getArchitectureUpdate(allStories);

        // WHEN
        List<YamlFeatureStory> actual = StoryPublishingService.getFeatureStoriesToCreate(au);

        // THEN
        assertThat(actual, equalTo(featureStoriesToBeCreated));
    }

    private YamlArchitectureUpdate getArchitectureUpdate(List<YamlFeatureStory> featureStories) {
        YamlCapabilitiesContainer capabilitiesContainer = new YamlCapabilitiesContainer(
                new YamlEpic("Epic Title", new YamlJira("AU-1", null)),
                featureStories
        );
        return YamlArchitectureUpdate.blank().toBuilder()
                .capabilityContainer(capabilitiesContainer)
                .build();
    }
}
