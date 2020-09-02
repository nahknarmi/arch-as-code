package net.trilogy.arch.domain.architectureUpdate;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArchitectureUpdateTest {

    @Test
    public void shouldAddJiraToFeatureStory() {
        // GIVEN:
        final var storyToChange = FeatureStory.blank().toBuilder().jira(new Jira("OLD JIRA TICKET 2", "OLD JIRA LINK 2")).build();

        final var originalAu = getAuWithStories(
                List.of(
                        FeatureStory.blank().toBuilder().jira(new Jira("OLD JIRA TICKET 1", "OLD JIRA LINK 1")).build(),
                        storyToChange,
                        FeatureStory.blank().toBuilder().jira(new Jira("OLD JIRA TICKET 3", "OLD JIRA LINK 3")).build()
                )
        );

        // WHEN:
        final var actual = originalAu.addJiraToFeatureStory(storyToChange, new Jira("NEW JIRA TICKET", "NEW JIRA LINK"));

        // THEN:
        final var expected = getAuWithStories(
                List.of(
                        FeatureStory.blank().toBuilder().jira(new Jira("OLD JIRA TICKET 1", "OLD JIRA LINK 1")).build(),
                        FeatureStory.blank().toBuilder().jira(new Jira("NEW JIRA TICKET", "NEW JIRA LINK")).build(),
                        FeatureStory.blank().toBuilder().jira(new Jira("OLD JIRA TICKET 3", "OLD JIRA LINK 3")).build()
                )
        );

        assertThat(actual, equalTo(expected));
    }

    private ArchitectureUpdate getAuWithStories(List<FeatureStory> stories) {
        return ArchitectureUpdate.builderPreFilledWithBlanks()
                .capabilityContainer(
                        CapabilitiesContainer.blank().toBuilder().featureStories(stories).build())
                .build();
    }
}
