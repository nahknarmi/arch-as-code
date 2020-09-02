package net.trilogy.arch.adapter.jira;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface RemoteJira {
    @GET("/browse/{jiraCardId}")
    @Headers({"Accept: application/json"})
    Call<RemoteJiraIssue> browseIssue(@Path("jiraCardId") final String jiraCardId);
}
