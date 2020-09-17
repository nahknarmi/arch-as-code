package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.NonNull;
import net.trilogy.arch.domain.architectureUpdate.Jira;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PACKAGE;
import static net.trilogy.arch.adapter.jira.JiraCreateStoryStatus.failed;
import static net.trilogy.arch.adapter.jira.JiraCreateStoryStatus.succeeded;

@Getter(PACKAGE)
public class JiraApi {
    @VisibleForTesting
    private final URI baseUri;
    @VisibleForTesting
    private final String getStoryEndpoint;
    @VisibleForTesting
    private final String bulkCreateEndpoint;
    @VisibleForTesting
    private final String linkPrefix;

    private final JiraRestClient jiraClient;

    public JiraApi(
            JiraRestClient jiraClient,
            URI baseUri,
            String getStoryEndpoint,
            String bulkCreateEndpoint,
            String linkPrefix) {
        this.jiraClient = jiraClient;
        this.baseUri = URI.create(baseUri.toString().replaceAll("/$", "") + "/");
        this.bulkCreateEndpoint = bulkCreateEndpoint.replaceAll("(^/|/$)", "") + "/";
        this.getStoryEndpoint = getStoryEndpoint.replaceAll("(^/|/$)", "") + "/";
        this.linkPrefix = linkPrefix.replaceAll("(^/|/$)", "") + "/";
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

    public static void main(final String... args) throws ExecutionException, InterruptedException {
        final var root = URI.create("http://jira.devfactory.com");
        final var issueKey = "AU-1";

        final String username;
        final String password;
        if (args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            System.err.println("REQUIRED: <username> <password>");
            System.exit(2);
            throw new Error("BUG");
        }

        final var client = new AsynchronousJiraRestClientFactory().create(
                root,
                new BasicHttpAuthenticationHandler(username, password));

        final var issue = client.getIssueClient().getIssue(issueKey).get();
        System.out.println("issue = " + issue);
    }
}
