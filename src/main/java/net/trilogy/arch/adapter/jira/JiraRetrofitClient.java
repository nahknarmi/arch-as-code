package net.trilogy.arch.adapter.jira;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

public class JiraRetrofitClient {
    private static final String trilogyJiraRestApiBaseUrl = "https://tw-trilogy.atlassian.net/rest/api/2/";

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

    public static Response<RemoteJiraIssue> browseIssue(final String issueId) throws IOException {
        // TODO: Use exceptions for non 2xx responses: See OpenFeign
        return REMOTE_JIRA.browseIssue(issueId).execute();
    }
}
