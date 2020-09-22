package net.trilogy.arch.services.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.adapter.jira.JiraApi;
import net.trilogy.arch.adapter.jira.JiraApi.JiraApiException;
import net.trilogy.arch.adapter.jira.JiraCreateStoryStatus;
import net.trilogy.arch.adapter.jira.JiraStory;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class StoryPublishingService {
    private final PrintWriter out;
    private final PrintWriter err;
    private final JiraApi api;

    public static List<YamlFeatureStory> getFeatureStoriesToCreate(final YamlArchitectureUpdate au) {
        return au.getCapabilityContainer()
                .getFeatureStories()
                .stream()
                .filter(StoryPublishingService::shouldCreateStory)
                .collect(toList());
    }

    private static YamlArchitectureUpdate updateJiraTicketsInAu(
            final YamlArchitectureUpdate au,
            final List<YamlFeatureStory> stories,
            final List<JiraCreateStoryStatus> creationStatuses) {
        YamlArchitectureUpdate updatedAu = au;
        for (int i = 0; i < creationStatuses.size(); ++i) {
            final var result = creationStatuses.get(i);

            if (result.isSuccess()) {
                updatedAu = updatedAu.addJiraToFeatureStory(
                        stories.get(i),
                        new YamlJira(result.getIssueKey(), result.getIssueLink()));
            }
        }
        return updatedAu;
    }

    private static boolean shouldCreateStory(YamlFeatureStory story) {
        return !story.exists();
    }

    public YamlArchitectureUpdate createStories(
            final YamlArchitectureUpdate au,
            final ArchitectureDataStructure beforeAuArchitecture,
            final ArchitectureDataStructure afterAuArchitecture)
            throws JiraApiException, NoStoriesToCreateException, JiraStory.InvalidStoryException {
        printStoriesNotToBeSent(au);

        final var stories = getFeatureStoriesToCreate(au);
        if (stories.size() == 0) {
            throw new NoStoriesToCreateException();
        }

        out.println("Checking epic...\n");

        final var epicJiraTicket = au.getCapabilityContainer().getEpic().getJira();
        final var informationAboutTheEpic = api.getStory(epicJiraTicket);

        out.println("Attempting to create stories...\n");

        final var jiraStories = new ArrayList<JiraStory>(stories.size());
        for (var story : stories) {
            jiraStories.add(new JiraStory(au, beforeAuArchitecture, afterAuArchitecture, story));
        }

        // create stories
        var createStoriesResults = api.createStories(
                jiraStories,
                epicJiraTicket.getTicket(),
                informationAboutTheEpic.getProjectId()
        );

        // update stories

        // delete stories

        printStoriesThatSucceeded(stories, createStoriesResults);
        printStoriesThatFailed(stories, createStoriesResults);

        return updateJiraTicketsInAu(au, stories, createStoriesResults);
    }

    private void printStoriesThatSucceeded(List<YamlFeatureStory> stories, List<JiraCreateStoryStatus> createStoriesResults) {
        StringBuilder successfulStories = new StringBuilder();

        for (int i = 0; i < createStoriesResults.size(); ++i) {
            if (!createStoriesResults.get(i).isSuccess()) continue;
            successfulStories.append("\n  - ").append(stories.get(i).getTitle());
        }

        String heading = "Successfully created:";

        if (!successfulStories.toString().isBlank()) {
            out.println(heading + successfulStories);
        }
    }

    private void printStoriesThatFailed(List<YamlFeatureStory> stories, List<JiraCreateStoryStatus> createStoriesResults) {
        StringBuilder errors = new StringBuilder();
        for (int i = 0; i < createStoriesResults.size(); ++i) {
            if (createStoriesResults.get(i).isSuccess()) continue;
            errors.append("Story: \"").append(stories.get(i).getTitle()).append("\":\n  - ").append(createStoriesResults.get(i).getError());
        }
        String heading = "Error! Some stories failed to publish. Please retry. Errors reported by Jira:";
        if (!errors.toString().isBlank()) {
            err.println("\n" + heading + "\n\n" + errors);
        }
    }

    private void printStoriesNotToBeSent(final YamlArchitectureUpdate au) {
        String stories = au.getCapabilityContainer()
                .getFeatureStories()
                .stream()
                .filter(story -> !shouldCreateStory(story))
                .map(story -> "  - " + story.getTitle())
                .collect(Collectors.joining("\n"));
        if (!stories.isBlank()) {
            out.println("Not re-creating stories:\n" + stories + "\n");
        }
    }

    public static class NoStoriesToCreateException extends Exception {
    }
}

