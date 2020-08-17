package net.trilogy.arch.adapter.jira;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.services.Base64Converter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JiraApi {
    private final HttpClient client;
    private final String baseUri;
    private final String getStoryEndpoint;
    private final String bulkCreateEndpoint;
    private final String linkPrefix;

    public JiraApi(HttpClient client, String baseUri, String getStoryEndpoint, String bulkCreateEndpoint, String linkPrefix) {
        this.client = client;
        this.baseUri = baseUri.replaceAll("/$", "") + "/";
        this.bulkCreateEndpoint = bulkCreateEndpoint.replaceAll("(^/|/$)", "") + "/";
        this.getStoryEndpoint = getStoryEndpoint.replaceAll("(^/|/$)", "") + "/";
        this.linkPrefix = linkPrefix.replaceAll("(^/|/$)", "") + "/";
    }

    public List<JiraCreateStoryStatus> createStories(List<JiraStory> jiraStories, String epicKey, String projectId, String projectKey, String username, char[] password) throws JiraApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(generateBodyForCreateStories(epicKey, jiraStories, projectId)))
                .header("Authorization", "Basic " + getEncodeAuth(username, password))
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUri + bulkCreateEndpoint))
                .build();

        HttpResponse<String> response = null;
        try {
            response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                throw JiraApiException.builder()
                        .message("Failed to log into Jira. Please check your credentials.")
                        .response(response)
                        .build();
            }

            return parseCreateStoriesResponse(response.body());
        } catch (JiraApiException e) {
            throw e;
        } catch (Throwable e) {
            throw JiraApiException.builder()
                    .cause(e)
                    .response(response)
                    .message("Unknown error occurred")
                    .build();
        }
    }

    private List<JiraCreateStoryStatus> parseCreateStoriesResponse(String response) {
        final JSONArray successfulItems = new JSONObject(response).getJSONArray("issues");
        final JSONArray failedItems = new JSONObject(response).getJSONArray("errors");

        final int totalElements = successfulItems.length() + failedItems.length();

        JiraCreateStoryStatus[] result = new JiraCreateStoryStatus[totalElements];

        for (int i = 0; i < failedItems.length(); ++i) {
            int indexOfFailedItem = failedItems.getJSONObject(i).getInt("failedElementNumber");
            String error = extractErrorFromJiraCreateStoryResult(failedItems.getJSONObject(i));
            result[indexOfFailedItem] = JiraCreateStoryStatus.failed(error);
        }

        for (int i = 0; i < successfulItems.length(); ++i) {
            String key = successfulItems.getJSONObject(i).getString("key");
            JiraCreateStoryStatus item = JiraCreateStoryStatus.succeeded(key, this.baseUri + this.linkPrefix + key);
            insertInNextAvailableSpot(result, totalElements, item);
        }

        return List.of(result);
    }

    private void insertInNextAvailableSpot(Object[] arrayToInsertInto, int sizeOfArray, Object itemToInsert) {
        for (int j = 0; j < sizeOfArray; ++j) {
            if (arrayToInsertInto[j] == null) {
                arrayToInsertInto[j] = itemToInsert;
                break;
            }
        }
    }

    private String extractErrorFromJiraCreateStoryResult(JSONObject jiraErrorObj) {
        final String errorMessages = jiraErrorObj.getJSONObject("elementErrors")
                .getJSONArray("errorMessages")
                .toList()
                .stream()
                .map(it -> it + "\n")
                .collect(Collectors.joining());

        JSONObject mappedErrorsAsJson = jiraErrorObj.getJSONObject("elementErrors")
                .getJSONObject("errors");

        String mappedErrorMessages = mappedErrorsAsJson.keySet()
                .stream()
                .map(it -> it + ": " + mappedErrorsAsJson.getString(it) + "\n")
                .collect(Collectors.joining());

        return errorMessages + mappedErrorMessages;
    }

    public JiraQueryResult getStory(Jira jira, String username, char[] password) throws JiraApiException {
        String encodedAuth = getEncodeAuth(username, password);
        final String ticket = jira.getTicket();
        final HttpRequest request = createGetStoryRequest(encodedAuth, ticket);

        HttpResponse<String> response = null;
        try {
            response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() == 401) {
                throw JiraApiException.builder()
                    .message("Failed to log into Jira. Please check your credentials.")
                    .response(response)
                    .build();
            }
            if(response.statusCode() == 404) {
                throw JiraApiException.builder()
                    .message("Story \""+ jira.getTicket() +"\" not found. URL: " + request.uri().toURL().toString())
                    .response(response)
                    .build();
            }

            return new JiraQueryResult(response);
        } catch(JiraApiException e) {
            throw e;
        } catch (Throwable e) {
            throw JiraApiException.builder()
                    .cause(e)
                    .response(response)
                    .message("Unknown error occurred")
                    .build();
        }
    }

    private String generateBodyForCreateStories(String epicKey, List<JiraStory> jiraStories, String projectId) {
        return new JSONObject(
                Map.of("issueUpdates", new JSONArray(
                        jiraStories.stream().map(story -> new JSONObject(Map.of(
                                "fields", Map.of(
                                        "customfield_10002", epicKey,
                                        "project", Map.of("id", projectId),
                                        "summary", story.getTitle(),
                                        "issuetype", Map.of("name", "Feature Story"),
                                        "description", makeDescription(story)
                                )))).collect(Collectors.toList())
                ))
        ).toString();
    }

    private String makeDescription(JiraStory story) {
        return "" +
                makeFunctionalRequirementTable(story) +
                makeTddTablesByComponent(story);
    }

    private String makeTddTablesByComponent(JiraStory story) {
        Map<String, List<JiraStory.JiraTdd>> compMap = story.getTdds()
                .stream()
                .collect(Collectors.groupingBy(JiraStory.JiraTdd::getComponentPath));

        return "h3. Technical Design:\n" +
                compMap.entrySet().stream().map(
                        entry -> "h4. Component: " + entry.getKey() + "\n||TDD||Description||\n" +
                                entry.getValue().stream().map(
                                        tdd -> "| " + tdd.getId() + " | {noformat}" + tdd.getText() + "{noformat} |\n"
                                ).collect(Collectors.joining())
                ).collect(Collectors.joining()) +
                "";
    }

    private String makeFunctionalRequirementTable(JiraStory story) {
        return "h3. Implements functionality:\n" +
                "||Id||Source||Description||\n" +
                story.getFunctionalRequirements()
                        .stream()
                        .map(this::makeFunctionalRequiremntRow)
                        .collect(Collectors.joining());
    }

    private String makeFunctionalRequiremntRow(JiraStory.JiraFunctionalRequirement funcReq) {
        return ""
                + "| " + funcReq.getId() + " | "
                + funcReq.getSource()
                + " | {noformat}" + funcReq.getText() + "{noformat} |\n"
                + "";
    }

    private String getEncodeAuth(String username, char[] password) {
        final String s = username + ":" + String.valueOf(password);
        return Base64Converter.toString(s);
    }

    private HttpRequest createGetStoryRequest(String encodedAuth, String ticket) {
        return HttpRequest
                .newBuilder()
                .GET()
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + encodedAuth)
                .uri(URI.create(baseUri + getStoryEndpoint + ticket))
                .build();
    }

    @VisibleForTesting
    HttpClient getHttpClient() {
        return client;
    }

    @VisibleForTesting
    String getBaseUri() {
        return baseUri;
    }

    @VisibleForTesting
    String getGetStoryEndpoint() {
        return getStoryEndpoint;
    }

    @VisibleForTesting
    String getBulkCreateEndpoint() {
        return bulkCreateEndpoint;
    }

    @VisibleForTesting
    String getLinkPrefix() {
        return linkPrefix;
    }

    @Getter
    public static class JiraApiException extends Exception {
        @NonNull private final String message;
        private final Throwable cause;
        private final HttpResponse<?> response;

        @Builder
        private JiraApiException(@NonNull String message, Throwable cause, HttpResponse<?> response) {
            this.message = message;
            this.cause = cause;
            this.response = response;
        }
    }
}
