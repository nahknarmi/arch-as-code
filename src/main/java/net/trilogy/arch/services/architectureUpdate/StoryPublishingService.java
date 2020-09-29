package net.trilogy.arch.services.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.adapter.jira.JiraApi;
import net.trilogy.arch.adapter.jira.JiraApiException;
import net.trilogy.arch.adapter.jira.JiraE2E;
import net.trilogy.arch.adapter.jira.JiraIssueConvertible;
import net.trilogy.arch.adapter.jira.JiraQueryResult;
import net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus;
import net.trilogy.arch.adapter.jira.JiraStory.InvalidStoryException;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;

import java.io.PrintWriter;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class StoryPublishingService {
    private final PrintWriter out;
    private final PrintWriter err;
    private final JiraApi api;

    public static List<JiraE2E> getE2EtoCreate(final YamlArchitectureUpdate au) {
       return au.getCapabilityContainer().getFeatureStories().stream()
               .map(s -> new JiraE2E(s, au)).collect(toList());
    }

    public YamlArchitectureUpdate processStories(
            final YamlArchitectureUpdate au)
            throws InvalidStoryException, JiraApiException {
        printStoriesToSkip(au);

        final var storiesToCreate = au.findJiraIssuesToCreate();
        final var storiesToUpdate = au.findJiraIssuesToUpdate();
        // TODO: storiesToDelete -- implies calling REST JIRA to check

        if (storiesToCreate.isEmpty() && storiesToUpdate.isEmpty()) {
            out.println("No stories to create or update.");
            return au;
        }

        final var yamlEpicJira = au.getCapabilityContainer().getEpic().getJira();
        final var informationAboutTheEpic = api.getStory(yamlEpicJira);

        final var createStoriesResults = createStories(
                storiesToCreate,
                yamlEpicJira,
                informationAboutTheEpic);

        final var updateStoriesResults = updateStories(
                storiesToUpdate,
                yamlEpicJira,
                informationAboutTheEpic);

        out.println();
        printStoriesThatSucceeded(storiesToCreate, createStoriesResults, "created");
        printStoriesThatFailed(createStoriesResults);
        printStoriesThatSucceeded(storiesToUpdate, updateStoriesResults, "updated");
        printStoriesThatFailed(updateStoriesResults);

        return au.amendJiraTicketsInAu(storiesToCreate, createStoriesResults);
    }

    private List<JiraRemoteStoryStatus> createStories(
            List<? extends JiraIssueConvertible> storiesToCreate,
            YamlJira yamlEpicJira,
            JiraQueryResult informationAboutTheEpic) throws InvalidStoryException, JiraApiException {
        if (storiesToCreate.isEmpty()) return emptyList();

        out.printf("Creating stories in the epic having JIRA key %s and project id %d...%n",
                informationAboutTheEpic.getProjectKey(),
                informationAboutTheEpic.getProjectId());

        return api.createJiraIssues(
                storiesToCreate,
                yamlEpicJira.getTicket(),
                informationAboutTheEpic.getProjectId());
    }

    private List<JiraRemoteStoryStatus> updateStories(
            List<? extends JiraIssueConvertible> storiesToUpdate,
            YamlJira yamlEpicJira,
            JiraQueryResult informationAboutTheEpic) throws InvalidStoryException {
        if (storiesToUpdate.isEmpty()) return emptyList();

        out.printf("Updating stories in the epic having JIRA key %s and project id %d...%n",
                informationAboutTheEpic.getProjectKey(),
                informationAboutTheEpic.getProjectId());

        return api.updateExistingStories(
                storiesToUpdate,
                yamlEpicJira.getTicket()
        );
    }

    private void printStoriesThatSucceeded(
            final List<? extends JiraIssueConvertible> stories,
            final List<JiraRemoteStoryStatus> results,
            final String tag) {
        StringBuilder successfulStories = new StringBuilder();

        for (int i = 0; i < results.size(); ++i) {
            if (!results.get(i).isSuccess()) continue;
            successfulStories.append("\n  - ").append(stories.get(i).title());
        }

        String heading = String.format("Successfully %s:", tag);

        if (!successfulStories.toString().isBlank()) {
            out.println(heading + successfulStories);
        }
    }

    private void printStoriesThatFailed(
            List<JiraRemoteStoryStatus> createStoriesResults) {
        StringBuilder errors = new StringBuilder();
        for (JiraRemoteStoryStatus createStoriesResult : createStoriesResults) {
            if (createStoriesResult.isSuccess()) continue;
            errors.append("Story: \"")
                    .append(createStoriesResult.getIssue().title())
                    .append("\":\n  - ")
                    .append(createStoriesResult.getError());
        }
        String heading = "Error! Some stories failed to publish. Please retry. Errors reported by Jira:";
        if (!errors.toString().isBlank()) {
            err.println("\n" + heading + "\n\n" + errors);
        }
    }

    private void printStoriesToSkip(final YamlArchitectureUpdate au) {
        // TODO: Fix user output on processing stories:
        //       - Should note stories to create
        //       - Should note stories to update
        //       - Should note stories to delete
        //       - Should note stories to ignore
        final var toSkip = au.getCapabilityContainer().getFeatureStories().stream()
                .filter(YamlFeatureStory::hasJiraKeyAndLink)
                .map(story -> "  - " + story.getTitle() + " (" + story.getKey() + ")")
                .collect(joining("\n"));
        if (!toSkip.isBlank()) {
            out.println("Not recreating stories:\n" + toSkip + "\n");
        }
    }
}

