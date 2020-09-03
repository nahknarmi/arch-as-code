package net.trilogy.arch.adapter.jira;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static net.trilogy.arch.adapter.jira.JiraRetrofitClient.browseIssue;
import static org.junit.Assert.assertEquals;

public class JiraRetrofitClientTest {
    @Ignore("TODO: Fix authentication")
    @Test
    public void find_an_existing_issue() throws IOException {
        final var issue = browseIssue("AAC-129");

        assertEquals(issue.toString(), issue.code(), 200);
    }
}
