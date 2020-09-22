package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import net.trilogy.arch.domain.architectureUpdate.YamlEpic;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.List;

import static io.atlassian.util.concurrent.Promises.promise;
import static java.util.Collections.emptyList;
import static net.trilogy.arch.adapter.jira.JiraApi.isEquivalentToJira;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    public void should_find_jira_and_yaml_cards_which_are_to_be_compared() {
        final var theKey = "AU-1";
        final var fromYaml = YamlFeatureStory.builder()
                .jira(YamlJira.builder()
                        .ticket(theKey)
                        .build())
                .build();

        final var fromJira = mock(Issue.class);
        when(fromJira.getKey()).thenReturn(theKey);

        assertEquals(fromYaml.getKey(), fromJira.getKey());
    }

    @Test
    public void should_find_jira_and_yaml_story_data_to_be_equivalent() {
        final var theTitle = "AUNT MARGARET";
        final var fromYaml = YamlFeatureStory.builder()
                .requirementReferences(List.of(new FunctionalRequirementId("ALICE")))
                .tddReferences(List.of(new TddId("BOB")))
                .title(theTitle)
                .build();

        final var fromJira = mock(Issue.class);
        // Alas, not using JDK 15 which has multi-line text blocks
        when(fromJira.getSummary()).thenReturn(theTitle);

        assertTrue(isEquivalentToJira(fromYaml, fromJira));
    }

    @Test
    public void should_find_jira_and_yaml_epic_data_to_be_equivalent() {
        final var theTitle = "JAVIER IS JEFE";
        final var fromYaml = YamlEpic.builder()
                .title(theTitle)
                .build();

        final var fromJira = mock(Issue.class);
        when(fromJira.getSummary()).thenReturn(theTitle);

        assertTrue(isEquivalentToJira(fromYaml, fromJira));
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
