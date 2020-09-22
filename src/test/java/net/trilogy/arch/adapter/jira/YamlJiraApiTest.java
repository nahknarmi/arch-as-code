package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import net.trilogy.arch.adapter.jira.JiraApi.JiraApiException;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.List;

import static io.atlassian.util.concurrent.Promises.promise;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YamlJiraApiTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private IssueRestClient mockIssueClient;
    private JiraApi jiraApi;

    @Before
    public void setUp() {
        final var mockJiraClient = mock(JiraRestClient.class);
        mockIssueClient = mock(IssueRestClient.class);
        when(mockJiraClient.getIssueClient()).thenReturn(mockIssueClient);

        jiraApi = new JiraApi(mockJiraClient);
    }

    @Test
    public void shouldMakeRequestToGetJiraStory() throws Exception {
        // GIVEN:
        final var mockIssue = mock(Issue.class);
        final var mockProject = mock(Project.class);
        when(mockIssueClient.getIssue(anyString())).thenReturn(promise(mockIssue));
        when(mockIssue.getProject()).thenReturn(mockProject);
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getKey()).thenReturn("B");

        final YamlJira jiraToQuery = new YamlJira("JIRA-TICKET-123", "http://link");

        // WHEN:
        final var queryResult = jiraApi.getStory(jiraToQuery);

        // THEN:
        assertEquals(new JiraQueryResult(1L, "B"), queryResult);
    }

    @Test
    public void shouldThrowProperExceptionIfGetStoryNotFound() {
        // GIVEN:
        final var clientException = new RestClientException(List.of(
                ErrorCollection.builder()
                        .errorMessage("Issue Does Not Exist")
                        .status(404)
                        .build()),
                404);
        when(mockIssueClient.getIssue(anyString())).thenThrow(clientException);

        try {
            // WHEN:
            jiraApi.getStory(new YamlJira("A", "B"));

            //THEN:
            fail("BUG: Exception not thrown");
        } catch (JiraApiException e) {
            collector.checkThat(e.getMessage(), equalTo("Story \"A\" not found. Issue: A"));
            collector.checkThat(e.getCause(), is(clientException));
        }
    }

    @Test
    public void shouldThrowProperExceptionIfUnauthorizedForCreateStory() {
        // GIVEN:
        final var clientException = new RestClientException(emptyList(), 401);
        when(mockIssueClient.getIssue(anyString())).thenThrow(clientException);

        try {
            // WHEN:
            jiraApi.getStory(new YamlJira("A", "B"));

            //THEN:
            fail("BUG: Exception not thrown");
        } catch (JiraApiException e) {
            collector.checkThat(e.getMessage(), equalTo("Failed to log into Jira. Please check your credentials."));
            collector.checkThat(e.getCause(), is(clientException));
        }
    }
}
