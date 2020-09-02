package net.trilogy.arch.adapter.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

import static java.net.http.HttpClient.Redirect.NORMAL;

public class JiraApiFactory {
    public static final String JIRA_API_SETTINGS_FILE_PATH = ".arch-as-code/jira/settings.json";
    public static final HttpClient JIRA_HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(NORMAL)
            .build();

    private static final ObjectMapper jsonReader = new ObjectMapper();

    public JiraApi create(FilesFacade files, Path rootDir) throws IOException {
        final var rawContents = files.readString(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH));
        final var baseUri = jsonReader.readTree(rawContents).get("base_uri").textValue();
        final var getStoryEndpoint = jsonReader.readTree(rawContents).get("get_story_endpoint").textValue();
        final var bulkCreateEndpoint = jsonReader.readTree(rawContents).get("bulk_create_endpoint").textValue();
        final var linkPrefix = jsonReader.readTree(rawContents).get("link_prefix").textValue();

        return new JiraApi(JIRA_HTTP_CLIENT, baseUri, getStoryEndpoint, bulkCreateEndpoint, linkPrefix);
    }
}
