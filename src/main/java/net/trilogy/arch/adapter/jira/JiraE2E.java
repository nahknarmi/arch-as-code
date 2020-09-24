package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlE2E;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalArea;

import static java.util.stream.Collectors.joining;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class JiraE2E implements JiraIssueConvertible {

    public static final String REGRESSION_SUITE = "45405";
    public static final String TEST_SUITE_CATEGORY = "customfield_23304";
    public static final String END_TO_END_TEST = "End-to-end Test";

    private final YamlE2E e2e;
    private final YamlFunctionalArea functionalArea;

    public JiraE2E(YamlFeatureStory featureStory, YamlArchitectureUpdate au) throws JiraStory.InvalidStoryException {
        this.e2e = featureStory.getE2e();
        functionalArea = au.getFunctionalAreas().get(this.e2e.getFunctionalAreaId());
        if (functionalArea == null) {
            throw new JiraStory.InvalidStoryException("Functional area not found for E2E:" + e2e.getTitle());
        }
    }

    public String title() {
        return e2e.getTitle();
    }

    @Override
    public String key() {
        return e2e.getJira().getTicket();
    }

    @Override
    public String link() {
        return e2e.getJira().getLink();
    }

    String businessGoal() {
        return "h3. Business Goal:\n" + "| " + this.e2e.getBusinessGoal() + " |\n\n";
    }

    String attributeRationale() {
        return "h3. Attribute Rationale:\n" + "| Attribute | Rationale |\n" +
                e2e.getAttributes().stream().map(a -> "| " + a.getName() + " | " + a.getRationale() + " |").collect(joining("\n"));
    }

    String makeDescription() {
        return businessGoal() +
            attributeRationale();
    }

    public IssueInput asNewIssueInput(String epicKey, Long projectId) {
        return new IssueInputBuilder()
                .setFieldValue(TEST_SUITE_CATEGORY, ComplexIssueInputFieldValue.with("id", REGRESSION_SUITE))
                .setFieldValue("project", ComplexIssueInputFieldValue.with("id", projectId))
                .setFieldValue("summary", e2e.getTitle())
                .setFieldValue("issuetype", ComplexIssueInputFieldValue.with("name", END_TO_END_TEST))
                .setFieldValue("description", makeDescription())
                .build();
    }

    public IssueInput asExistingIssueInput(String epicKey) {
        return new IssueInputBuilder()
                .setFieldValue(TEST_SUITE_CATEGORY, ComplexIssueInputFieldValue.with("id", REGRESSION_SUITE))
                .setFieldValue("issuetype", ComplexIssueInputFieldValue.with("name", END_TO_END_TEST))
                .setFieldValue("summary", e2e.getTitle())
                .setFieldValue("description", makeDescription())
                .build();
    }
}
