package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.YamlEpic;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.SUMMARY_FIELD;
import static java.lang.System.err;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus.failed;
import static net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus.succeeded;

@RequiredArgsConstructor
public class JiraApi {
    private final JiraRestClient jiraClient;

    /**
     * Note: The Epic link is stored in field 10002 (or 10004), and seems to
     * use either the Epic title or key.  Key is <strong>strongly</strong>
     * preferred.
     *
     * @todo Test the Description field; update if changed
     * @todo Ensure the Epic field (10002 / 10004) points to the right key;
     * update otherwise
     */
    public static boolean isEquivalentToJira(YamlFeatureStory fromYaml, Issue fromJira) {
        requireNonNull(fromYaml);
        requireNonNull(fromJira);

        return Objects.equals(fromYaml.getTitle(), fromJira.getSummary());
    }

    /**
     * The only field which can be compared for Epic cards in JIRA is the
     * "Summary" (title).  An Epic card has no links to it's Story cards;
     * instead, the Story cards have a backlink to the Epic via
     * "customfield_10002" or "customfield_10004" fields.  And we do not write
     * out a "Description" field.
     *
     * @todo Which is better, "customfield_10002" or "customfield_10004"?
     * @see JiraStory#asIssueInput(String, Long)
     */
    public static boolean isEquivalentToJira(YamlEpic fromYaml, Issue fromJira) {
        requireNonNull(fromYaml);
        requireNonNull(fromJira);

        return Objects.equals(fromYaml.getTitle(), fromJira.getSummary());
    }

    public JiraQueryResult getStory(YamlJira jira)
            throws JiraApiException {
        final var ticket = jira.getTicket();

        try {
            final var issue = jiraClient.getIssueClient()
                    .getIssue(ticket).get();

            // TODO: ICK -- why are we only checking the project ID and Key?
            // TODO: This needs to return the full issue so that we can compare
            //       it against the YAML, and decide if it needs updating
            return new JiraQueryResult(issue.getProject().getId(), issue.getProject().getKey());
        } catch (RestClientException e) {
            // TODO: Tech debt: Use JDK Optional, not Guava's look-a-like
            //       Root cause: Atlassian library is behind the curve
            final var code = e.getStatusCode();
            if (!code.isPresent()) throw e;

            switch (code.get()) {
                case 401:
                    throw new JiraApiException(
                            "Failed to log into Jira. Please check your credentials.",
                            e);
                case 404:
                    throw new JiraApiException(
                            "Story \"" + jira.getTicket() + "\" not found. Issue: " + ticket,
                            e);
                default:
                    throw new JiraApiException(
                            "Unknown error occurred: " + e.getMessage(),
                            e);
            }
        } catch (InterruptedException e) {
            throw new JiraApiException("INTERRUPTED", e);
        } catch (ExecutionException e) {
            throw new JiraApiException("FAILED", e.getCause());
        }
    }

    public List<JiraRemoteStoryStatus> createNewStories(
            List<JiraStory> jiraStories,
            String epicKey,
            Long projectId)
            throws JiraApiException {
        try {
            return getJiraCreateStoryStatuses(jiraStories, epicKey, projectId);
        } catch (RestClientException e) {
            final var x = new JiraApiException(e.getMessage(), e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        } catch (InterruptedException e) {
            final var x = new JiraApiException("INTERRUPTED", e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        } catch (ExecutionException e) {
            final var x = new JiraApiException("FAILED", e.getCause());
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }

    private List<JiraRemoteStoryStatus> getJiraCreateStoryStatuses(
            List<JiraStory> jiraStories,
            String epicKey,
            Long projectId) throws InterruptedException, ExecutionException {
        final var jiraIssues = jiraStories.stream()
                .map(it -> it.asIssueInput(epicKey, projectId))
                .collect(toList());
        final var bulkResponse = jiraClient.getIssueClient()
                .createIssues(jiraIssues)
                .get();

        final var succeeded = stream(bulkResponse.getIssues().spliterator(), false)
                .map(it -> succeeded(it.getKey(), it.getSelf().toString()))
                .collect(toList());
        final var failed = stream(bulkResponse.getErrors().spliterator(), false)
                .map(it -> failed(it.toString()))
                .collect(toList());

        final var result = new ArrayList<JiraRemoteStoryStatus>(succeeded.size() + failed.size());
        result.addAll(succeeded);
        result.addAll(failed);
        return result;
    }

    public List<JiraRemoteStoryStatus> updateExistingStories(
            List<JiraStory> jiraStories,
            String epicKey,
            Long projectId) {
        return jiraStories.stream()
                .map(it -> updateOneExistingStory(it, epicKey, projectId))
                .collect(toList());
    }

    private JiraRemoteStoryStatus updateOneExistingStory(
            JiraStory story,
            String epicKey,
            Long projectId) {
        try {
            final var input = story.asIssueInput(epicKey, projectId);
            jiraClient.getIssueClient().updateIssue(story.getKey(), input).get();
            return succeeded(story.getKey(), story.getLink());
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            return failed(e.toString());
        } catch (final ExecutionException e) {
            return failed(e.getCause().toString());
        }
    }

    @Generated
    public static void main(final String... args) throws ExecutionException, InterruptedException {
        final var root = URI.create("https://jira.devfactory.com");
        final var epicKey = "AU-1";

        final String username;
        final String password;
        if (args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            err.println("REQUIRED: <username> <password>");
            System.exit(2);
            throw new Error("BUG");
        }

        final var client = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(root, username, password);
        final var issues = client.getIssueClient();

        final var start = System.currentTimeMillis();
        try {
            issues.updateIssue(epicKey, IssueInput.createWithFields(
                    new FieldInput(SUMMARY_FIELD, "JAVIER IS JEFE")
            )).get();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final var end = System.currentTimeMillis();
        err.println("TIME -> " + (end - start));

        final var issue = issues.getIssue(epicKey).get();
        out.println("issue = " + issue);
    }
}
