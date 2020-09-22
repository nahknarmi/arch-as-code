package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import lombok.SneakyThrows;
import net.trilogy.arch.adapter.jira.JiraApi.JiraApiException;
import net.trilogy.arch.domain.architectureUpdate.Epic;
import net.trilogy.arch.domain.architectureUpdate.FeatureStory;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.net.URI;
import java.util.List;

import static io.atlassian.util.concurrent.Promises.promise;
import static java.util.Collections.emptyList;
import static net.trilogy.arch.adapter.jira.JiraApi.isEquivalentToJiraIssue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraApiTest {
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
    public void should_find_jira_and_yaml_stories_which_are_to_be_compared() {
        final var theKey = "AU-1";
        final var story = FeatureStory.builder()
                .jira(Jira.builder()
                        .ticket(theKey)
                        .build())
                .build();

        final var issue = mock(Issue.class);
        when(issue.getKey()).thenReturn(theKey);

        assertEquals(story.getKey(), issue.getKey());
    }

    @SneakyThrows
    @Test
    public void should_find_jira_and_yaml_jira_structure_to_be_equivalent() {
        final var ticket = Jira.builder()
                .link("https://jira.devfactory.com/browse/AU-1")
                .ticket("AU-1")
                .build();

        final var issue = mock(Issue.class);
        when(issue.getKey()).thenReturn("AU-1");
        when(issue.getSelf()).thenReturn(new URI("https://jira.devfactory.com/browse/AU-1"));

        assertTrue(isEquivalentToJiraIssue(ticket, issue));
    }

    @SneakyThrows
    @Test
    public void should_find_jira_and_yaml_epic_structure_to_be_equivalent() {
        final var theKey = "AU-1";
        final var theLink = "https://jira.devfactory.com/browse/" + theKey;
        final var theTitle = "JAVIER IS JEFE";
        final var ticket = Epic.builder()
                .jira(Jira.builder()
                        .ticket(theKey)
                        .link(theLink)
                        .build())
                .title(theTitle)
                .build();

        final var issue = mock(Issue.class);
        when(issue.getKey()).thenReturn(theKey);
        when(issue.getSummary()).thenReturn(theTitle);
        when(issue.getSelf()).thenReturn(new URI(theLink));

        assertTrue(isEquivalentToJiraIssue(ticket, issue));
    }

    @Test
    public void should_find_jira_and_yaml_feature_story_cards_to_be_equivalent() {
        final var theKey = "INTENTIONALLY RIGHT";
        final var theLink = "scheme:rest-of-uri";
        final var theTitle = "AUNT MARGARET";

        final var story = FeatureStory.builder()
                .jira(Jira.builder()
                        .link(theLink)
                        .ticket(theKey)
                        .build())
                .title(theTitle)
                .build();

        final var issue = mock(Issue.class);
        when(issue.getKey()).thenReturn(theKey);
        when(issue.getSelf()).thenReturn(URI.create(theLink));
        when(issue.getSummary()).thenReturn(theTitle);

        assertTrue(isEquivalentToJiraIssue(story, issue));
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

        final Jira jiraToQuery = new Jira("JIRA-TICKET-123", "http://link");

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
            jiraApi.getStory(new Jira("A", "B"));

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
            jiraApi.getStory(new Jira("A", "B"));

            //THEN:
            fail("BUG: Exception not thrown");
        } catch (JiraApiException e) {
            collector.checkThat(e.getMessage(), equalTo("Failed to log into Jira. Please check your credentials."));
            collector.checkThat(e.getCause(), is(clientException));
        }
    }
}
