package net.trilogy.arch.adapter.jira;

import retrofit2.Response;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

public class JiraClient {
    private static final String trilogyBaseUrl = "https://tw-trilogy.atlassian.net/";

    private static final RemoteJira REMOTE_JIRA = new Builder()
            .baseUrl(trilogyBaseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(RemoteJira.class);

    public static Response<RemoteJiraIssue> browseIssue(final String issueId) throws IOException {
        // TODO: Use exceptions for non 2xx responses: See OpenFeign
        return REMOTE_JIRA.browseIssue(issueId).execute();
    }
}
