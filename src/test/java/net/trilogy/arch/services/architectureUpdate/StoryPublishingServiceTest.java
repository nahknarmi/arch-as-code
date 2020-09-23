package net.trilogy.arch.services.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlCapabilitiesContainer;
import net.trilogy.arch.domain.architectureUpdate.YamlE2E;
import net.trilogy.arch.domain.architectureUpdate.YamlEpic;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static net.trilogy.arch.services.architectureUpdate.StoryPublishingService.findFeatureStoriesToCreate;
import static net.trilogy.arch.services.architectureUpdate.StoryPublishingService.findFeatureStoriesToUpdate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StoryPublishingServiceTest {
    @Test
    public void should_find_list_to_send_to_jira() {
        // GIVEN
        final var featureStoriesToBeCreated = List.of(
                new YamlFeatureStory("Story Title",
                        new YamlJira("", ""),
                        List.of(new TddId("TDD 1.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ")),
                        YamlE2E.blank()),
                new YamlFeatureStory("Story Title",
                        new YamlJira(null, null),
                        List.of(new TddId("TDD 2.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ 2")),
                        YamlE2E.blank()),
                new YamlFeatureStory("Story Title",
                        null,
                        List.of(new TddId("TDD 3.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ 3")),
                        YamlE2E.blank()));
        final var featureStoriesToBeUpdated = List.of(
                new YamlFeatureStory("Story Exists - Do Not Create",
                        new YamlJira("some ticket", "some link"),
                        List.of(new TddId("TDD 2.0")),
                        List.of(new FunctionalRequirementId("FUNC REQ")),
                        YamlE2E.blank()));

        final var allStories = new ArrayList<YamlFeatureStory>(
                featureStoriesToBeCreated.size()
                        + featureStoriesToBeUpdated.size());
        allStories.addAll(featureStoriesToBeCreated);
        allStories.addAll(featureStoriesToBeUpdated);
        final var au = createArchitectureUpdate(allStories);

        // WHEN
        final var actualCreate = findFeatureStoriesToCreate(au);
        final var actualUpdate = findFeatureStoriesToUpdate(au);

        // THEN
        assertThat(actualCreate, equalTo(featureStoriesToBeCreated));
        assertThat(actualUpdate, equalTo(featureStoriesToBeUpdated));
    }

    private static YamlArchitectureUpdate createArchitectureUpdate(List<YamlFeatureStory> featureStories) {
        final var capabilitiesContainer = new YamlCapabilitiesContainer(
                new YamlEpic("Epic Title", new YamlJira("AU-1", null)),
                featureStories);

        return YamlArchitectureUpdate.blank().toBuilder()
                .capabilityContainer(capabilitiesContainer)
                .build();
    }
}
