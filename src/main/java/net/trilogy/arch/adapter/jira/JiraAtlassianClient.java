package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class JiraAtlassianClient {
    private static final String trilogyJiraBaseUrl = "https://tw-trilogy.atlassian.net";

    private static final JiraRestClient ATLASSIAN_JIRA_CLIENT = newAtlassianJiraClient();

    private static JiraRestClient newAtlassianJiraClient() {
        final var trilogyJiraBaseUri = URI.create(trilogyJiraBaseUrl);

        return new AsynchronousJiraRestClient(
                trilogyJiraBaseUri,
                new AsynchronousHttpClientFactory().createClient(trilogyJiraBaseUri,
                        new BasicHttpAuthenticationHandler("MY_PASSWORD", "MY_TOKEN")
                )
        );
    }

    public static Issue getIssue(final String issueId)
            throws ExecutionException, InterruptedException {
        return ATLASSIAN_JIRA_CLIENT.getIssueClient().getIssue(issueId).get();
    }
}
