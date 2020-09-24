package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.domain.input.IssueInput;

public interface JiraIssueConvertible {

    IssueInput asNewIssueInput(String epicKey, Long projectId);

}
