package net.trilogy.arch.services.architectureUpdate;

import net.trilogy.arch.adapter.jira.*;
import net.trilogy.arch.adapter.jira.JiraStory.JiraFunctionalRequirement;
import net.trilogy.arch.adapter.jira.JiraStory.JiraTdd;
import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;
import static net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus.succeeded;
import static net.trilogy.arch.services.architectureUpdate.StoryPublishingService.FUNCTIONAL_AREA_COVERAGE;
import static net.trilogy.arch.services.architectureUpdate.StoryPublishingService.TESTS_WRITING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

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

    @Test
    public void linkNewlyCreateStoryAndNewCreatedE2E() {
        JiraApi jiraApi = mock(JiraApi.class);
        List<JiraFunctionalRequirement> functionalRequirements = List.of(new JiraFunctionalRequirement(FunctionalRequirementId.blank(), YamlFunctionalRequirement.blank()));
        List<YamlAttribute> attributes = List.of(new YamlAttribute("", "", new YamlJira("at-id", "at-link")));
        YamlFeatureStory featureStory = YamlFeatureStory.blank().toBuilder().e2e(new YamlE2E("", "", YamlFunctionalArea.FunctionalAreaId.blank(), null, attributes)).build();
        JiraStory jiraStory = new JiraStory(featureStory, List.of(new JiraTdd(TddId.noPr(), YamlTdd.blank(), "path", null)), functionalRequirements);
        JiraE2E jiraE2E = new JiraE2E(featureStory.getE2e(), new YamlFunctionalArea("fn area", new YamlJira("fn-a-id", "fn-a-url")));
        List<JiraRemoteStoryStatus> createStoriesResults = List.of(
                succeeded("AU-11", "https://AU-11" , jiraStory),
                succeeded("AU-12", "https://AU-12", jiraE2E));

        new StoryPublishingService(mock(PrintWriter.class), mock(PrintWriter.class), jiraApi).linkJiraIssues(createStoriesResults);

        verify(jiraApi).linkIssue("AU-11", "AU-12", TESTS_WRITING);
        verify(jiraApi).linkIssue("fn-a-id", "AU-12", FUNCTIONAL_AREA_COVERAGE);
        verify(jiraApi).linkIssue("at-id", "AU-12", FUNCTIONAL_AREA_COVERAGE);
    }

    @Test
    public void linkNewCreatedE2EWithAttributesAndFunctionalArea() {
        JiraApi jiraApi = mock(JiraApi.class);
        List<YamlAttribute> attributes = List.of(new YamlAttribute("", "", new YamlJira("at-id", "at-link")));
        YamlFeatureStory featureStory = YamlFeatureStory.blank().toBuilder().e2e(new YamlE2E("", "", YamlFunctionalArea.FunctionalAreaId.blank(), null, attributes)).build();
        JiraE2E jiraE2E = new JiraE2E(featureStory.getE2e(), new YamlFunctionalArea("fn area", new YamlJira("fn-a-id", "fn-a-url")));
        List<JiraRemoteStoryStatus> createStoriesResults = List.of(
                succeeded("AU-12", "https://AU-12", jiraE2E));

        new StoryPublishingService(mock(PrintWriter.class), mock(PrintWriter.class), jiraApi).linkJiraIssues(createStoriesResults);

        verify(jiraApi).linkIssue("fn-a-id", "AU-12", FUNCTIONAL_AREA_COVERAGE);
        verify(jiraApi).linkIssue("at-id", "AU-12", FUNCTIONAL_AREA_COVERAGE);
    }

    @Test
    public void linkNewlyCreateStoryAndExistingE2E() {
        JiraApi jiraApi = mock(JiraApi.class);
        List<JiraFunctionalRequirement> functionalRequirements = List.of(new JiraFunctionalRequirement(FunctionalRequirementId.blank(), YamlFunctionalRequirement.blank()));
        List<YamlAttribute> attributes = List.of(new YamlAttribute("", "", new YamlJira("at-id", "at-link")));
        YamlFeatureStory featureStory = YamlFeatureStory.blank().toBuilder().e2e(new YamlE2E("", "", YamlFunctionalArea.FunctionalAreaId.blank(), new YamlJira("AU-12", "https://AU-12"), attributes)).build();
        JiraStory jiraStory = new JiraStory(featureStory, List.of(new JiraTdd(TddId.noPr(), YamlTdd.blank(), "path", null)), functionalRequirements);
        List<JiraRemoteStoryStatus> createStoriesResults = List.of(
                succeeded("AU-11", "https://AU-11" , jiraStory));

        new StoryPublishingService(mock(PrintWriter.class), mock(PrintWriter.class), jiraApi).linkJiraIssues(createStoriesResults);

        verify(jiraApi).linkIssue("AU-11", "AU-12", TESTS_WRITING);
    }


    @Test
    public void notLinkExistingStoryAndExistingE2E() {
        JiraApi jiraApi = mock(JiraApi.class);

        new StoryPublishingService(mock(PrintWriter.class), mock(PrintWriter.class), jiraApi).linkJiraIssues(List.of());

        verify(jiraApi, times(0)).linkIssue("AU-11", "AU-12", TESTS_WRITING);
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
