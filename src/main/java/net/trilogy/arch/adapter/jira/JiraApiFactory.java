package net.trilogy.arch.adapter.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

public class JiraApiFactory {
    public static final String JIRA_API_SETTINGS_FILE_PATH = ".arch-as-code/jira/settings.json";

    private HttpClient client;

    public JiraApi create(FilesFacade files, Path rootDir) throws IOException {
        var rawContents = files.readString(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH));
        final ObjectMapper objectMapper = new ObjectMapper();
        String baseUri = objectMapper.readTree(rawContents).get("base_uri").textValue();
        String getStoryEndpoint = objectMapper.readTree(rawContents).get("get_story_endpoint").textValue();
        String bulkCreateEndpoint = objectMapper.readTree(rawContents).get("bulk_create_endpoint").textValue();
        String linkPrefix = objectMapper.readTree(rawContents).get("link_prefix").textValue();
        client = createClient();

        return new JiraApi(client, baseUri, getStoryEndpoint, bulkCreateEndpoint, linkPrefix);
    }

    @VisibleForTesting
    HttpClient createClient() {
        if (client == null) {
            client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }

        return client;
    }
}
