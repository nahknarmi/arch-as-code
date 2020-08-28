package net.trilogy.arch.services.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.*;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StoryPublishingServiceTest {

    @Test
    public void shouldListStoriesToBeSentToJira() {
        // GIVEN
        List<FeatureStory> featureStories = List.of(
                new FeatureStory("Story Title",
                        new Jira("", ""),
                        List.of(new Tdd.Id("TDD 1.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ"))
                ), new FeatureStory("Story Title",
                        new Jira(null, null),
                        List.of(new Tdd.Id("TDD 2.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ 2"))
                ), new FeatureStory("Story Title",
                        null,
                        List.of(new Tdd.Id("TDD 3.0")),
                        List.of(new FunctionalRequirement.Id("FUNC REQ 3"))
                )
        );
        ArchitectureUpdate au = getArchitectureUpdate(featureStories);

        // WHEN
        List<FeatureStory> actual = StoryPublishingService.getFeatureStoriesToCreate(au);

        // THEN
        assertThat(actual, equalTo(featureStories));
    }

    private ArchitectureUpdate getArchitectureUpdate(List<FeatureStory> featureStories) {
        CapabilitiesContainer capabilitiesContainer = new CapabilitiesContainer(
                new Epic("Epic Title", new Jira("AU-1", null)),
                featureStories
        );
        ArchitectureUpdate au = ArchitectureUpdate.blank().toBuilder()
                .capabilityContainer(capabilitiesContainer)
                .build();
        return au;
    }
}
