package net.trilogy.arch.adapter.jira;

import lombok.Data;

@Data
public class JiraQueryResult {
    private final String projectId;
    private final String projectKey;
}
