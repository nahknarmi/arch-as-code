package net.trilogy.arch.adapter.Jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import net.trilogy.arch.adapter.FilesFacade;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

public class JiraApiFactory {
    public static final String JIRA_API_SETTINGS_FILE_PATH = ".arch-as-code/jira/settings.json";

    private HttpClient client;

    public JiraApi create(FilesFacade files) throws IOException {
        var rawContents = files.readString(Path.of(JIRA_API_SETTINGS_FILE_PATH));
        final ObjectMapper objectMapper = new ObjectMapper();
        String baseUri = objectMapper.readTree(rawContents).get("base_uri").textValue();
        String bulkCreateEndpoint = objectMapper.readTree(rawContents).get("bulk_create_endpoint").textValue();
        this.client = createClient();

        return new JiraApi(this.client, baseUri, bulkCreateEndpoint);
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
