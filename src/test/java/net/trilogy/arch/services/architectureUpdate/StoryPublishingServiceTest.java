package net.trilogy.arch.services.architectureUpdate;

import net.trilogy.arch.adapter.jira.JiraE2E;
import net.trilogy.arch.adapter.jira.JiraIssueConvertible;
import net.trilogy.arch.adapter.jira.JiraStory;
import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;
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
                        List.of(FunctionalRequirementId.blank()),
                        YamlE2E.blank()),
                new YamlFeatureStory("Story Title",
                        new YamlJira(null, null),
                        List.of(new TddId("TDD 2.0")),
                        List.of(FunctionalRequirementId.blank()),
                        YamlE2E.blank()),
                new YamlFeatureStory("Story Title",
                        null,
                        List.of(new TddId("TDD 3.0")),
                        List.of(FunctionalRequirementId.blank()),
                        YamlE2E.blank()));

        final var featureStoriesToBeUpdated = List.of(
                new YamlFeatureStory("Story Exists - Do Not Create",
                        new YamlJira("some ticket", "some link"),
                        List.of(new TddId("TDD 2.0")),
                        List.of(FunctionalRequirementId.blank()),
                        YamlE2E.blank()));

        final var allStories = new ArrayList<YamlFeatureStory>(
                featureStoriesToBeCreated.size()
                        + featureStoriesToBeUpdated.size());
        allStories.addAll(featureStoriesToBeCreated);
        allStories.addAll(featureStoriesToBeUpdated);
        final var au = createArchitectureUpdate(allStories);

        List<JiraIssueConvertible> jiraStoriesToCreate = featureStoriesToBeCreated.stream().map(s -> new JiraStory(s, au)).collect(toList());

        List<JiraIssueConvertible> jiraStoriesToUpdate = List.of(new JiraStory(featureStoriesToBeUpdated.get(0), au),
                new JiraE2E(YamlE2E.blank(), au.getFunctionalAreas().get(YamlFunctionalArea.FunctionalAreaId.blank())),
                new JiraE2E(YamlE2E.blank(), au.getFunctionalAreas().get(YamlFunctionalArea.FunctionalAreaId.blank())),
                new JiraE2E(YamlE2E.blank(), au.getFunctionalAreas().get(YamlFunctionalArea.FunctionalAreaId.blank())),
                new JiraE2E(YamlE2E.blank(), au.getFunctionalAreas().get(YamlFunctionalArea.FunctionalAreaId.blank())));

        // WHEN
        final var actualCreate = au.findJiraIssuesToCreate();
        final var actualUpdate = au.findJiraIssuesToUpdate();

        // THEN
        assertThat(actualCreate, equalTo(jiraStoriesToCreate));
        assertThat(actualUpdate, equalTo(jiraStoriesToUpdate));
    }

    private static YamlArchitectureUpdate createArchitectureUpdate(List<YamlFeatureStory> featureStories) {
        final var capabilitiesContainer = new YamlCapabilitiesContainer(
                new YamlEpic("Epic Title", new YamlJira("AU-1", "epic link")),
                featureStories);
        var au =YamlArchitectureUpdate.blank();
        var tdds = new HashMap<TddId, YamlTdd>();
        featureStories.stream().forEach( fs -> {
            fs.getTddReferences().stream().forEach(tddId -> {
                tdds.put(tddId, YamlTdd.blank());
            });
        });
        YamlTddContainerByComponent container = au.getTddContainersByComponent().get(0).toBuilder().tdds(tdds).build();
        return au.toBuilder()
                .tddContainersByComponent(singletonList(container))
                .capabilityContainer(capabilitiesContainer)
                .build();
    }
}
