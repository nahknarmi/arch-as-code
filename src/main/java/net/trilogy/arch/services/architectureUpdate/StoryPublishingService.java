package net.trilogy.arch.services.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.adapter.jira.JiraApi;
import net.trilogy.arch.adapter.jira.JiraApiException;
import net.trilogy.arch.adapter.jira.JiraQueryResult;
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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public class StoryPublishingService {
    private final PrintWriter out;
    private final PrintWriter err;
    private final JiraApi api;

    public YamlArchitectureUpdate processStories(
            final YamlArchitectureUpdate au,
            final ArchitectureDataStructure beforeAuArchitecture,
            final ArchitectureDataStructure afterAuArchitecture)
            throws InvalidStoryException, JiraApiException {
        printStoriesToSkip(au);

        final var storiesToCreate = au.findFeatureStoriesToCreate();
        final var storiesToUpdate = au.findFeatureStoriesToUpdate();
        // TODO: storiesToDelete -- implies calling REST JIRA to check

        if (storiesToCreate.isEmpty() && storiesToUpdate.isEmpty()) {
            out.println("No stories to create or update.");
            return au;
        }

        final var yamlEpicJira = au.getCapabilityContainer().getEpic().getJira();
        final var informationAboutTheEpic = api.getStory(yamlEpicJira);

        final var createStoriesResults = createStories(
                au,
                beforeAuArchitecture,
                afterAuArchitecture,
                storiesToCreate,
                yamlEpicJira,
                informationAboutTheEpic);

        final var updateStoriesResults = updateStories(
                au,
                beforeAuArchitecture,
                afterAuArchitecture,
                storiesToUpdate,
                yamlEpicJira,
                informationAboutTheEpic);

        out.println();
        printStoriesThatSucceeded(storiesToCreate, createStoriesResults, "created");
        printStoriesThatFailed(storiesToCreate, createStoriesResults);
        printStoriesThatSucceeded(storiesToUpdate, updateStoriesResults, "updated");
        printStoriesThatFailed(storiesToUpdate, updateStoriesResults);

        final var processedStories = new ArrayList<YamlFeatureStory>(
                storiesToCreate.size() + storiesToUpdate.size());
        processedStories.addAll(storiesToCreate);
        processedStories.addAll(storiesToUpdate);
        final var processResults = new ArrayList<JiraRemoteStoryStatus>(
                createStoriesResults.size() + updateStoriesResults.size());
        processResults.addAll(createStoriesResults);
        processResults.addAll(updateStoriesResults);

        return au.amendJiraTicketsInAu(processedStories, processResults);
    }

    private List<JiraRemoteStoryStatus> createStories(
            YamlArchitectureUpdate au,
            ArchitectureDataStructure beforeAuArchitecture,
            ArchitectureDataStructure afterAuArchitecture,
            List<YamlFeatureStory> storiesToCreate,
            YamlJira yamlEpicJira,
            JiraQueryResult informationAboutTheEpic) throws InvalidStoryException, JiraApiException {
        if (storiesToCreate.isEmpty()) return emptyList();

        out.printf("Creating stories in the epic having JIRA key %s and project id %d...%n",
                informationAboutTheEpic.getProjectKey(),
                informationAboutTheEpic.getProjectId());
        // TODO: Exception thrown in ctor for JiraStory prevents use of Stream
        final var jiraStoriesToCreate = new ArrayList<JiraStory>(storiesToCreate.size());
        for (final YamlFeatureStory story : storiesToCreate) {
            jiraStoriesToCreate.add(new JiraStory(story, au, beforeAuArchitecture, afterAuArchitecture));
        }

        return api.createNewStories(
                jiraStoriesToCreate,
                yamlEpicJira.getTicket(),
                informationAboutTheEpic.getProjectId());
    }

    private List<JiraRemoteStoryStatus> updateStories(
            YamlArchitectureUpdate au,
            ArchitectureDataStructure beforeAuArchitecture,
            ArchitectureDataStructure afterAuArchitecture,
            List<YamlFeatureStory> storiesToUpdate,
            YamlJira yamlEpicJira,
            JiraQueryResult informationAboutTheEpic) throws InvalidStoryException {
        if (storiesToUpdate.isEmpty()) return emptyList();

        out.printf("Updating stories in the epic having JIRA key %s and project id %d...%n",
                informationAboutTheEpic.getProjectKey(),
                informationAboutTheEpic.getProjectId());
        final var jiraStoriesToUpdate = new ArrayList<JiraStory>(storiesToUpdate.size());
        for (final YamlFeatureStory story : storiesToUpdate) {
            jiraStoriesToUpdate.add(new JiraStory(story, au, beforeAuArchitecture, afterAuArchitecture));
        }

        return api.updateExistingStories(
                jiraStoriesToUpdate,
                yamlEpicJira.getTicket()
        );
    }

    private void printStoriesThatSucceeded(
            final List<YamlFeatureStory> stories,
            final List<JiraRemoteStoryStatus> results,
            final String tag) {
        StringBuilder successfulStories = new StringBuilder();

        for (int i = 0; i < results.size(); ++i) {
            if (!results.get(i).isSuccess()) continue;
            successfulStories.append("\n  - ").append(stories.get(i).getTitle());
        }

        String heading = String.format("Successfully %s:", tag);

        if (!successfulStories.toString().isBlank()) {
            out.println(heading + successfulStories);
        }
    }

    private void printStoriesThatFailed(
            List<YamlFeatureStory> stories,
            List<JiraRemoteStoryStatus> createStoriesResults) {
        StringBuilder errors = new StringBuilder();
        for (int i = 0, x = createStoriesResults.size(); i < x; ++i) {
            if (createStoriesResults.get(i).isSuccess()) continue;
            errors.append("Story: \"")
                    .append(stories.get(i).getTitle())
                    .append("\":\n  - ")
                    .append(createStoriesResults.get(i).getError());
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

