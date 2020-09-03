package net.trilogy.arch.adapter.jira;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static net.trilogy.arch.adapter.jira.JiraAtlassianClient.getIssue;

public class JiraAtlassianClientTest {
    @Test
    public void find_an_existing_issue() throws ExecutionException, InterruptedException {
        getIssue("AAC-129");
    }
}
