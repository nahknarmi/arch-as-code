package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;

public class JiraAtlassianClient {
    private static final String trilogyJiraBaseUrl = "https://tw-trilogy.atlassian.net";

    private static final JiraRestClient ATLASSIAN_JIRA_CLIENT = newAtlassianJiraClient();

    private static JiraRestClient newAtlassianJiraClient() {
        final var trilogyJiraBaseUri = URI.create(trilogyJiraBaseUrl);

        return new AsynchronousJiraRestClient(
                trilogyJiraBaseUri,
                new AsynchronousHttpClientFactory().createClient(trilogyJiraBaseUri,
                        new BasicHttpAuthenticationHandler("MY_USERNAME", "MY_PASSWORD")
                )
        );
    }

    public static Issue getIssue(final String issueId)
            throws ExecutionException, InterruptedException {
        return ATLASSIAN_JIRA_CLIENT.getIssueClient().getIssue(issueId).get();
    }
}
