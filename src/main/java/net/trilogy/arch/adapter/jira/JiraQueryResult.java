package net.trilogy.arch.adapter.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.net.http.HttpResponse;

@Getter
public class JiraQueryResult {
    private final String projectId;
    private final String projectKey;

    public JiraQueryResult(String projectId, String projectKey) {
        this.projectId = projectId;
        this.projectKey = projectKey;
    }

    public JiraQueryResult(HttpResponse<String> response) throws JsonProcessingException {
        JsonNode json = new ObjectMapper().readValue(response.body(), JsonNode.class);
        this.projectId = json.get("fields").get("project").get("id").asText();
        this.projectKey = json.get("fields").get("project").get("key").asText();
    }
}
