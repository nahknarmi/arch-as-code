package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import net.trilogy.arch.domain.architectureUpdate.YamlE2E;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalArea;
import org.junit.Test;

import java.util.Map;

import static net.trilogy.arch.adapter.jira.JiraE2E.*;
import static org.junit.Assert.assertEquals;

public class JiraE2ETest {

    @Test
    public void createsAttributeRationaleTable() {
        String attributeTable = new JiraE2E(YamlE2E.blank(), YamlFunctionalArea.blank()).attributeRationale();
        assertEquals("h3. Attribute Rationale:\n" +
                "| Attribute | Rationale |\n" +
                "| Accessible | UI need to be Accessible |", attributeTable);
    }

    @Test
    public void createsBusinessGoalsTable() {
        String businessGoal = new JiraE2E(YamlE2E.blank(), YamlFunctionalArea.blank()).businessGoal();
        assertEquals("h3. Business Goal:\n" +
                "| Need to do this |\n\n", businessGoal);
    }

    @Test
    public void createsIssueInput() {
        JiraE2E jiraE2E = new JiraE2E(YamlE2E.blank(), YamlFunctionalArea.blank());
        IssueInput issueInput = jiraE2E.asNewIssueInput("Epic", 123L);
        Map<String, FieldInput> fields = issueInput.getFields();
        assertEquals(new FieldInput(TEST_SUITE_CATEGORY, ComplexIssueInputFieldValue.with("id", REGRESSION_SUITE)), fields.get(TEST_SUITE_CATEGORY));
        assertEquals(new FieldInput("project", ComplexIssueInputFieldValue.with("id", 123L)), fields.get("project"));
        assertEquals(new FieldInput("summary", jiraE2E.getE2e().getTitle()), fields.get("summary"));
        assertEquals(new FieldInput("issuetype", ComplexIssueInputFieldValue.with("name", END_TO_END_TEST)), fields.get("issuetype"));
        assertEquals(new FieldInput("description", "h3. Business Goal:\n" +
                "| Need to do this |\n\n" +
                "h3. Attribute Rationale:\n" +
                "| Attribute | Rationale |\n" +
                "| Accessible | UI need to be Accessible |"), fields.get("description"));
    }

}
