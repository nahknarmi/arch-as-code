package net.trilogy.arch.services.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.adapter.jira.JiraApi;
import net.trilogy.arch.adapter.jira.JiraApiException;
import net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus;
import net.trilogy.arch.adapter.jira.JiraStory;
import net.trilogy.arch.adapter.jira.JiraStory.InvalidStoryException;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class StoryPublishingService {
    private final PrintWriter out;
    private final PrintWriter err;
    private final JiraApi api;

    public static List<YamlFeatureStory> findFeatureStoriesToCreate(final YamlArchitectureUpdate au) {
        return au.getCapabilityContainer().getFeatureStories().stream()
                .filter(story -> !story.exists())
                .collect(toList());
    }

    public static List<YamlFeatureStory> findFeatureStoriesToUpdate(final YamlArchitectureUpdate au) {
        return au.getCapabilityContainer().getFeatureStories().stream()
                .filter(YamlFeatureStory::exists)
                .collect(toList());
    }

    private static YamlArchitectureUpdate updateJiraTicketsInAu(
            final YamlArchitectureUpdate au,
            final List<YamlFeatureStory> stories,
            final List<JiraRemoteStoryStatus> creationStatuses) {
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

    public YamlArchitectureUpdate createOrUpdateStories(
            final YamlArchitectureUpdate au,
            final ArchitectureDataStructure beforeAuArchitecture,
            final ArchitectureDataStructure afterAuArchitecture)
            throws InvalidStoryException, JiraApiException {
        printStoriesToSkip(au);

        final var storiesToCreate = findFeatureStoriesToCreate(au);
        final var storiesToUpdate = findFeatureStoriesToUpdate(au);
        // TODO: storiesToDelete -- implies calling REST JIRA to check

        final var yamlEpicJira = au.getCapabilityContainer().getEpic().getJira();
        final var informationAboutTheEpic = api.getStory(yamlEpicJira);

        out.printf("Creating stories in the epic having JIRA key %s and project id %d...%n",
                informationAboutTheEpic.getProjectKey(),
                informationAboutTheEpic.getProjectId());

        // TODO: Exception thrown in ctor for JiraStory prevents use of Stream
        final var jiraStoriesToCreate = new ArrayList<JiraStory>(storiesToCreate.size());
        for (final YamlFeatureStory story : storiesToCreate) {
            jiraStoriesToCreate.add(new JiraStory(story, au, beforeAuArchitecture, afterAuArchitecture));
        }
        final var jiraStoriesToUpdate = new ArrayList<JiraStory>(storiesToUpdate.size());
        for (final YamlFeatureStory story : storiesToUpdate) {
            jiraStoriesToUpdate.add(new JiraStory(story, au, beforeAuArchitecture, afterAuArchitecture));
        }

        // create stories
        var createStoriesResults = api.createNewStories(
                jiraStoriesToCreate,
                yamlEpicJira.getTicket(),
                informationAboutTheEpic.getProjectId());
        // update stories
        var updateStoriesResults = api.updateExistingStories(
                jiraStoriesToUpdate,
                yamlEpicJira.getTicket(),
                informationAboutTheEpic.getProjectId());
        // delete stories

        final var createdOrUpdatedResults = new ArrayList<JiraRemoteStoryStatus>(
                createStoriesResults.size() + updateStoriesResults.size());
        createdOrUpdatedResults.addAll(createStoriesResults);
        createdOrUpdatedResults.addAll(updateStoriesResults);

        out.println();
        printStoriesThatSucceeded(storiesToCreate, createdOrUpdatedResults);
        printStoriesThatFailed(storiesToCreate, createdOrUpdatedResults);

        return updateJiraTicketsInAu(au, storiesToCreate, createdOrUpdatedResults);
    }

    private void printStoriesThatSucceeded(List<YamlFeatureStory> stories, List<JiraRemoteStoryStatus> createStoriesResults) {
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

    private void printStoriesThatFailed(List<YamlFeatureStory> stories, List<JiraRemoteStoryStatus> createStoriesResults) {
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

    private void printStoriesToSkip(final YamlArchitectureUpdate au) {
        // TODO: Fix user output on processing stories:
        //       - Should note stories to create
        //       - Should note stories to update
        //       - Should note stories to delete
        //       - Should note stories to ignore
        String stories = au.getCapabilityContainer().getFeatureStories().stream()
                .filter(YamlFeatureStory::exists)
                .map(story -> "  - " + story.getTitle() + " (" + story.getKey() + ")")
                .collect(joining("\n"));
        if (!stories.isBlank()) {
            out.println("Not recreating stories:\n" + stories + "\n");
        }
    }
}

