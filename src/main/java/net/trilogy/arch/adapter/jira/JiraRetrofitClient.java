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

public class JiraRetrofitClient {
    private static final String trilogyJiraBaseUrl = "https://tw-trilogy.atlassian.net";
    private static final String trilogyJiraRestApiBaseUrl = format("%s/rest/api/2/", trilogyJiraBaseUrl);

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

    private static final RemoteRetrofitJira REMOTE_JIRA = new Builder()
            .baseUrl(trilogyJiraRestApiBaseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .client(new OkHttpClient.Builder()
                    .authenticator((route, response) -> {
                        final var credential = Credentials.basic("a username", "a password");

                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    })
                    .build())
            .build()
            .create(RemoteRetrofitJira.class);

    public static Issue atlassianBrowseIssue(final String issueId)
            throws ExecutionException, InterruptedException {
        return ATLASSIAN_JIRA_CLIENT.getIssueClient().getIssue(issueId).get();
    }

    public static Response<RemoteJiraIssue> browseIssue(final String issueId)
            throws IOException {
        // TODO: Use exceptions for non 2xx responses: See OpenFeign
        return REMOTE_JIRA.browseIssue(issueId).execute();
    }
}
