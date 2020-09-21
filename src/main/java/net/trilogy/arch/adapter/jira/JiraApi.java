package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.Generated;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.SUMMARY_FIELD;
import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.trilogy.arch.adapter.jira.JiraCreateStoryStatus.failed;
import static net.trilogy.arch.adapter.jira.JiraCreateStoryStatus.succeeded;

@RequiredArgsConstructor
public class JiraApi {
    private final JiraRestClient jiraClient;

    public static boolean isEquivalentToJiraIssue(Jira item, Issue issue) {
        return item != null && issue != null &&
                Objects.equals(item.getLink(), issue.getSelf().toString());
    }

    public static boolean isEquivalentToJiraIssue(FeatureStory item, Issue issue) {
        return  item != null && issue != null
                && Objects.equals(item.getKey(), issue.getKey())
                && Objects.equals(item.getTitle(), issue.getSummary())
                && isEquivalentToJiraIssue(item.getJira(), issue);
    }

    public static boolean isEquivalentToJiraIssue(Epic item, Issue issue) {
        return item != null && issue != null
                && Objects.equals(item.getTitle(), issue.getSummary())
                && isEquivalentToJiraIssue(item.getJira(), issue);
    }

    public JiraQueryResult getStory(Jira jira)
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

    public List<JiraCreateStoryStatus> createStories(
            List<JiraStory> jiraStories,
            String epicKey,
            Long projectId)
            throws JiraApiException {
        try {
            final var bulkResponse = jiraClient.getIssueClient()
                    .createIssues(jiraStories.stream()
                            .map(aac -> aac.toJira(epicKey, projectId))
                            .collect(toList()))
                    .get();

            final var succeeded = stream(bulkResponse.getIssues().spliterator(), false)
                    .map(it -> succeeded(it.getKey(), it.getSelf().toString()))
                    .collect(toList());
            final var failed = stream(bulkResponse.getErrors().spliterator(), false)
                    .map(it -> failed(it.toString()))
                    .collect(toList());

            final var result = new ArrayList<JiraCreateStoryStatus>(succeeded.size() + failed.size());
            result.addAll(succeeded);
            result.addAll(failed);
            return result;
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

    public static class JiraApiException extends Exception {
        public JiraApiException(@NonNull String message, Throwable cause) {
            super(message, cause);
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
