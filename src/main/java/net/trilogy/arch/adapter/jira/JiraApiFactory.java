package net.trilogy.arch.adapter.jira;

import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

@UtilityClass
public class JiraApiFactory {
    public static final String JIRA_API_SETTINGS_FILE_PATH = ".arch-as-code/jira/settings.json";

    public static JiraApi newJiraApi(FilesFacade files, Path rootDir, String username, char[] password) throws IOException {
        var rawContents = files.readString(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH));
        final ObjectMapper objectMapper = new ObjectMapper();
        final var baseUri = URI.create(objectMapper.readTree(rawContents).get("base_uri").textValue());

        final var jiraClient = new AsynchronousJiraRestClientFactory().create(
                baseUri,
                new BasicHttpAuthenticationHandler(username, new String(password)));

        return new JiraApi(jiraClient);
    }
}
