package net.trilogy.arch.services.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.CapabilitiesContainer;
import net.trilogy.arch.domain.architectureUpdate.Epic;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
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
        List<FeatureStory> featureStoriesToBeCreated = List.of(
                new FeatureStory("Story Title",
                        new Jira("", ""),
                        List.of(new TddId("TDD 1.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ"))
                ), new FeatureStory("Story Title",
                        new Jira(null, null),
                        List.of(new TddId("TDD 2.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ 2"))
                ), new FeatureStory("Story Title",
                        null,
                        List.of(new TddId("TDD 3.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ 3"))
                )
        );
        List<FeatureStory> featureStoriesToNotBeCreated = List.of(
                new FeatureStory("Story Exists - Do Not Create",
                        new Jira("some ticket", "some link"),
                        List.of(new TddId("TDD 2.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ"))
                )
        );

        List<FeatureStory> allStories = Stream.concat(featureStoriesToBeCreated.stream(), featureStoriesToNotBeCreated.stream()).collect(Collectors.toList());
        ArchitectureUpdate au = getArchitectureUpdate(allStories);

        // WHEN
        List<FeatureStory> actual = StoryPublishingService.getFeatureStoriesToCreate(au);

        // THEN
        assertThat(actual, equalTo(featureStoriesToBeCreated));
    }

    private ArchitectureUpdate getArchitectureUpdate(List<FeatureStory> featureStories) {
        CapabilitiesContainer capabilitiesContainer = new CapabilitiesContainer(
                new Epic("Epic Title", new Jira("AU-1", null)),
                featureStories
        );
        return ArchitectureUpdate.blank().toBuilder()
                .capabilityContainer(capabilitiesContainer)
                .build();
    }
}
