package net.trilogy.arch.domain.architectureUpdate;

import org.junit.Test;

import java.util.List;

import static net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate.prefilledYamlArchitectureUpdateWithBlanks;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class YamlArchitectureUpdateTest {
    private static YamlArchitectureUpdate getAuWithStories(List<YamlFeatureStory> stories) {
        return prefilledYamlArchitectureUpdateWithBlanks()
                .capabilityContainer(
                        YamlCapabilitiesContainer.blank().toBuilder().featureStories(stories).build())
                .build();
    }

    @Test
    public void shouldAddJiraToFeatureStory() {
        // GIVEN:
        final var storyToChange = YamlFeatureStory.blank().toBuilder().jira(new YamlJira("OLD JIRA TICKET 2", "OLD JIRA LINK 2")).build();

        final var originalAu = getAuWithStories(
                List.of(
                        YamlFeatureStory.blank().toBuilder().jira(new YamlJira("OLD JIRA TICKET 1", "OLD JIRA LINK 1")).build(),
                        storyToChange,
                        YamlFeatureStory.blank().toBuilder().jira(new YamlJira("OLD JIRA TICKET 3", "OLD JIRA LINK 3")).build()
                )
        );

        // WHEN:
        final var actual = originalAu.addJiraToFeatureStory(storyToChange, new YamlJira("NEW JIRA TICKET", "NEW JIRA LINK"));

        // THEN:
        final var expected = getAuWithStories(
                List.of(
                        YamlFeatureStory.blank().toBuilder().jira(new YamlJira("OLD JIRA TICKET 1", "OLD JIRA LINK 1")).build(),
                        YamlFeatureStory.blank().toBuilder().jira(new YamlJira("NEW JIRA TICKET", "NEW JIRA LINK")).build(),
                        YamlFeatureStory.blank().toBuilder().jira(new YamlJira("OLD JIRA TICKET 3", "OLD JIRA LINK 3")).build()
                )
        );

        assertThat(actual, equalTo(expected));
    }
}
