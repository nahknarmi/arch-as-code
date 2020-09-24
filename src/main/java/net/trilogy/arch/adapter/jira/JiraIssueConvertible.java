package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.domain.input.IssueInput;

public interface JiraIssueConvertible {

    String title();

    String key();

    String link();

    IssueInput asNewIssueInput(String epicKey, Long projectId);

    IssueInput asExistingIssueInput(String epicKey);


}
