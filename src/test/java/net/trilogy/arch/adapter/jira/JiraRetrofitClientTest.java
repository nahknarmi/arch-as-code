package net.trilogy.arch.adapter.jira;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static net.trilogy.arch.adapter.jira.JiraRetrofitClient.atlassianBrowseIssue;
import static net.trilogy.arch.adapter.jira.JiraRetrofitClient.browseIssue;
import static org.junit.Assert.assertEquals;

public class JiraRetrofitClientTest {
    @Test
    public void find_an_existing_issue() throws IOException, ExecutionException, InterruptedException {
        final var atlassianIssue = atlassianBrowseIssue("AAC-129");
        final var issue = browseIssue("AAC-129");

        System.out.println("atlassianIssue = " + atlassianIssue);

        assertEquals(issue.toString(), 200, issue.code());
    }
}
