package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import static java.lang.System.out;

public class JiraAtlassianClient {
    private static final JiraRestClient liveClient =
            new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
                    URI.create("https://tw-trilogy.atlassian.net"),
                    "MY_PASSWORD",
                    "MY_TOKEN");

    private final JiraRestClient client;

    public JiraAtlassianClient() {
        client = liveClient;
    }

    /** For live prototyping and spiking. */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        out.println(new JiraAtlassianClient().getIssue("AAC-129"));
    }

    /** @todo Avoid the duplication with Atlassian's library */
    public Issue getIssue(final String issueId)
            throws ExecutionException, InterruptedException {
        return client.getIssueClient().getIssue(issueId).get();
    }
}
