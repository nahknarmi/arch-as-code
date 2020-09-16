package net.trilogy.arch.adapter.jira;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.services.Base64Converter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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

    private static void insertInNextAvailableSpot(Object[] arrayToInsertInto, int sizeOfArray, Object itemToInsert) {
        for (int j = 0; j < sizeOfArray; ++j) {
            if (arrayToInsertInto[j] == null) {
                arrayToInsertInto[j] = itemToInsert;
                break;
            }
        }
    }

    private static String extractErrorFromJiraCreateStoryResult(JSONObject jiraErrorObj) {
        final var errorMessages = jiraErrorObj.getJSONObject("elementErrors")
                .getJSONArray("errorMessages")
                .toList()
                .stream()
                .map(it -> it + "\n")
                .collect(joining());

        final var mappedErrorsAsJson = jiraErrorObj.getJSONObject("elementErrors")
                .getJSONObject("errors");

        final var mappedErrorMessages = mappedErrorsAsJson.keySet()
                .stream()
                .map(it -> it + ": " + mappedErrorsAsJson.getString(it) + "\n")
                .collect(joining());

        return errorMessages + mappedErrorMessages;
    }

    private static String buildTddRow(JiraStory.JiraTdd tdd) {
        if (tdd.hasTddContent()) {
            return "| " + tdd.getId() + " | " + tdd.getText() + " |\n";
        } else {
            return "| " + tdd.getId() + " | {noformat}" + tdd.getText() + "{noformat} |\n";
        }
    }

    private static String makeFunctionalRequirementRow(JiraStory.JiraFunctionalRequirement funcReq) {
        return ""
                + "| " + funcReq.getId() + " | "
                + funcReq.getSource()
                + " | {noformat}" + funcReq.getText() + "{noformat} |\n"
                + "";
    }

    private static String getEncodeAuth(String username, char[] password) {
        final var s = username + ":" + String.valueOf(password);

        return Base64Converter.toString(s);
    }

    private static String makeDescription(JiraStory story) {
        return "" +
                makeFunctionalRequirementTable(story) +
                makeTddTablesByComponent(story);
    }

    private static String makeTddTablesByComponent(JiraStory story) {
        final var compMap = story.getTdds()
                .stream()
                .collect(Collectors.groupingBy(JiraStory.JiraTdd::getComponentPath));

        return "h3. Technical Design:\n" +
                compMap.entrySet().stream()
                        .map(it ->
                                "h4. Component: " + it.getKey() + "\n||TDD||Description||\n" +
                                        it.getValue().stream()
                                                .map(JiraApi::buildTddRow)
                                                .collect(joining()))
                        .collect(joining());
    }

    private static String makeFunctionalRequirementTable(JiraStory story) {
        return "h3. Implements functionality:\n" +
                "||Id||Source||Description||\n" +
                story.getFunctionalRequirements().stream()
                        .map(JiraApi::makeFunctionalRequirementRow)
                        .collect(joining());
    }

    private static String generateBodyForCreateStories(String epicKey, List<JiraStory> jiraStories, String projectId) {
        return new JSONObject(
                Map.of("issueUpdates", new JSONArray(
                        jiraStories.stream()
                                .map(story -> new JSONObject(Map.of(
                                        "fields", Map.of(
                                                "customfield_10002", epicKey,
                                                "project", Map.of("id", projectId),
                                                "summary", story.getTitle(),
                                                "issuetype", Map.of("name", "Feature Story"),
                                                "description", makeDescription(story)))))
                                .collect(toList()))))
                .toString();
    }

    public List<JiraCreateStoryStatus> createStories(List<JiraStory> jiraStories, String epicKey, String projectId, String username, char[] password) throws JiraApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(generateBodyForCreateStories(epicKey, jiraStories, projectId)))
                .header("Authorization", "Basic " + getEncodeAuth(username, password))
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUri + bulkCreateEndpoint))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
        final var successfulItems = new JSONObject(response).getJSONArray("issues");
        final var failedItems = new JSONObject(response).getJSONArray("errors");
        final var totalElements = successfulItems.length() + failedItems.length();

        final var result = new JiraCreateStoryStatus[totalElements];

        for (int i = 0; i < failedItems.length(); ++i) {
            final var indexOfFailedItem = failedItems.getJSONObject(i).getInt("failedElementNumber");
            final var error = extractErrorFromJiraCreateStoryResult(failedItems.getJSONObject(i));

            result[indexOfFailedItem] = JiraCreateStoryStatus.failed(error);
        }

        for (int i = 0; i < successfulItems.length(); ++i) {
            final var key = successfulItems.getJSONObject(i).getString("key");
            final var item = JiraCreateStoryStatus.succeeded(key, baseUri + linkPrefix + key);

            insertInNextAvailableSpot(result, totalElements, item);
        }

        return List.of(result);
    }

    public JiraQueryResult getStory(Jira jira, String username, char[] password) throws JiraApiException {
        final var encodedAuth = getEncodeAuth(username, password);
        final var ticket = jira.getTicket();
        final var request = createGetStoryRequest(encodedAuth, ticket);

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                throw JiraApiException.builder()
                        .message("Failed to log into Jira. Please check your credentials.")
                        .response(response)
                        .build();
            }
            if (response.statusCode() == 404) {
                throw JiraApiException.builder()
                        .message("Story \"" + jira.getTicket() + "\" not found. URL: " + request.uri().toURL().toString())
                        .response(response)
                        .build();
            }

            return new JiraQueryResult(response);
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

    @Builder
    @Getter
    @RequiredArgsConstructor
    public static class JiraApiException extends Exception {
        @NonNull
        private final String message;
        private final Throwable cause;
        private final HttpResponse<?> response;
    }
}
