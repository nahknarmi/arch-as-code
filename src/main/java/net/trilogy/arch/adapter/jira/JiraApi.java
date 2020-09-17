package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.services.Base64Converter;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static lombok.AccessLevel.PACKAGE;

@Getter(PACKAGE)
@VisibleForTesting
public class JiraApi {
    private final URI baseUri;
    private final String getStoryEndpoint;
    private final String bulkCreateEndpoint;
    private final String linkPrefix;

    private final JiraRestClient jiraClient;

    public JiraApi(
            JiraRestClient jiraClient,
            URI baseUri,
            String getStoryEndpoint,
            String bulkCreateEndpoint,
            String linkPrefix) {
        this.jiraClient = jiraClient;
        this.baseUri = URI.create(baseUri.toString().replaceAll("/$", "") + "/");
        this.bulkCreateEndpoint = bulkCreateEndpoint.replaceAll("(^/|/$)", "") + "/";
        this.getStoryEndpoint = getStoryEndpoint.replaceAll("(^/|/$)", "") + "/";
        this.linkPrefix = linkPrefix.replaceAll("(^/|/$)", "") + "/";
    }

    public static void main(final String... args) throws ExecutionException, InterruptedException {
        final var root = URI.create("http://jira.devfactory.com");

        final String username;
        final String password;
        if (args.length == 2) {
            username = args[0];
            password = args[1];
        } else {
            System.err.println("REQUIRED: <username> <password>");
            System.exit(2);
            throw new Error("BUG");
        }

        final var client = new AsynchronousJiraRestClientFactory().create(
                root,
                new BasicHttpAuthenticationHandler(username, password));

        final var issue = client.getIssueClient().getIssue("AU-1").get();
        System.out.println("issue = " + issue);
    }

    public JiraQueryResult getStory(Jira jira)
            throws JiraApiException {
        final var ticket = jira.getTicket();

        try {
            final var issue = jiraClient.getIssueClient()
                    .getIssue(ticket).get();

            // TODO: ICK -- why are we only checking the project ID and Key?
            // TODO: This needs to return the full issue so that we can compare
            //       it against the YAML, and decide if it needs updating
            return new JiraQueryResult(issue.getProject().getId().toString(), issue.getProject().getKey());
        } catch (RestClientException e) {
            final var code = e.getStatusCode();
            if (!code.isPresent()) throw e;

            switch (code.get()) {
                case 401:
                    throw JiraApiException.builder()
                            .message("Failed to log into Jira. Please check your credentials.")
                            .build();
                case 404:
                    throw JiraApiException.builder()
                            .message("Story \"" + jira.getTicket() + "\" not found. Issue: " + ticket)
                            .build();
                default:
                    throw JiraApiException.builder()
                            .cause(e)
                            .message("Unknown error occurred")
                            .build();
            }
        } catch (InterruptedException e) {
            throw new JiraApiException("INTERRUPTED", e, null);
        } catch (ExecutionException e) {
            throw new JiraApiException("FAILED", e.getCause(), null);
        }
    }

    public List<JiraCreateStoryStatus> createStories(List<JiraStory> jiraStories, String epicKey, String projectId, String username, char[] password)
            throws JiraApiException {
        try {
            final var bulkResponse = jiraClient.getIssueClient()
                    .createIssues(jiraStories.stream()
                            .map(aac -> aac.toJira(epicKey, projectId))
                            .collect(toList()))
                    .get();

            final var succeeded = stream(bulkResponse.getIssues().spliterator(), false)
                    .map(it -> JiraCreateStoryStatus.succeeded(it.getKey(), it.getSelf().toString()))
                    .collect(toList());
            final var failed = stream(bulkResponse.getErrors().spliterator(), false)
                    .map(it -> JiraCreateStoryStatus.failed(it.toString()))
                    .collect(toList());

            final var result = new ArrayList<JiraCreateStoryStatus>(succeeded.size() + failed.size());
            result.addAll(succeeded);
            result.addAll(failed);
            return result;
        } catch (InterruptedException e) {
            throw new JiraApiException("INTERRUPTED", e, null);
        } catch (ExecutionException e) {
            throw new JiraApiException("FAILED", e.getCause(), null);
        }
    }

    private List<JiraCreateStoryStatus> parseCreateStoriesResponse(String response) {
        final var successfulItems = new JSONObject(response).getJSONArray("issues");
        final var failedItems = new JSONObject(response).getJSONArray("errors");
        final var totalElements = successfulItems.length() + failedItems.length();

        // TODO: Why, why?  Just make an ArrayList and add elements to the end!!
        final var result = new JiraCreateStoryStatus[totalElements];

        for (int i = 0; i < failedItems.length(); ++i) {
            final var indexOfFailedItem = failedItems.getJSONObject(i).getInt("failedElementNumber");
            final var error = extractErrorFromJiraCreateStoryResult(failedItems.getJSONObject(i));

            result[indexOfFailedItem] = JiraCreateStoryStatus.failed(error);
        }

        for (int i = 0; i < successfulItems.length(); ++i) {
            final var key = successfulItems.getJSONObject(i).getString("key");
            final var item = JiraCreateStoryStatus.succeeded(key, baseUri + linkPrefix + key);

            insertInNextAvailableSpot(result, item);
        }

        return List.of(result);
    }

    private static void insertInNextAvailableSpot(Object[] arrayToInsertInto, Object itemToInsert) {
        final int sizeOfArray = arrayToInsertInto.length;
        for (int i = 0; i < sizeOfArray; ++i) {
            if (null == arrayToInsertInto[i]) {
                arrayToInsertInto[i] = itemToInsert;
                break;
            }
        }
        // TODO: Funky algo -- what happens if we reach then end without inserting?
    }

    private static String extractErrorFromJiraCreateStoryResult(JSONObject jiraErrorObj) {
        final var errorMessages = jiraErrorObj.getJSONObject("elementErrors")
                .getJSONArray("errorMessages")
                .toList().stream()
                .map(it -> it + "\n")
                .collect(joining());

        final var mappedErrorsAsJson = jiraErrorObj.getJSONObject("elementErrors")
                .getJSONObject("errors");

        final var mappedErrorMessages = mappedErrorsAsJson.keySet().stream()
                .map(it -> it + ": " + mappedErrorsAsJson.getString(it) + "\n")
                .collect(joining());

        return errorMessages + mappedErrorMessages;
    }

    private static String getEncodeAuth(String username, char[] password) {
        final var s = username + ":" + String.valueOf(password);

        return Base64Converter.toString(s);
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
