package net.trilogy.arch.adapter.jira;

import lombok.Data;

@Data
public class JiraQueryResult {
    private final Long projectId;
    private final String projectKey;
}
