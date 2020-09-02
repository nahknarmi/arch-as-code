package net.trilogy.arch.adapter.jira;

import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static java.net.http.HttpClient.Redirect.NORMAL;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_API_SETTINGS_FILE_PATH;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_HTTP_CLIENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraApiFactoryTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    private final String expectedBaseUri = "BASE-URI/";
    private final String expectedGetStoryEndpoint = "GET-STORY-ENDPOINT/";
    private final String expectedBulkCreateEndpoint = "BULK-CREATE-ENDPOINT/";
    private final String expectedLinkPrefix = "LINK-PREFIX/";
    private FilesFacade mockedFiles;
    private Path rootDir;

    @Before
    public void setUp() throws Exception {
        rootDir = Path.of("a", "random", "root", "directory");
        String json = "" +
                "{\n" +
                "    \"base_uri\": \"" + expectedBaseUri + "\",\n" +
                "    \"link_prefix\": \"" + expectedLinkPrefix + "\",\n" +
                "    \"get_story_endpoint\": \"" + expectedGetStoryEndpoint + "\",\n" +
                "    \"bulk_create_endpoint\": \"" + expectedBulkCreateEndpoint + "\"\n" +
                "}";
        mockedFiles = mock(FilesFacade.class);
        when(
                mockedFiles.readString(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH))
        ).thenReturn(json);
    }

    @Test
    public void shouldUseTheRightConstants() {
        assertThat(JIRA_API_SETTINGS_FILE_PATH, equalTo(".arch-as-code/jira/settings.json"));
    }

    @Test
    public void shouldCreateJiraApiWithCorrectClient() throws IOException {
        final JiraApiFactory factory = new JiraApiFactory();
        JiraApi jiraApi = factory.create(mockedFiles, rootDir);

        collector.checkThat(jiraApi.getHttpClient(), is(JIRA_HTTP_CLIENT));
        collector.checkThat(jiraApi.getBaseUri(), equalTo(expectedBaseUri));
        collector.checkThat(jiraApi.getGetStoryEndpoint(), equalTo(expectedGetStoryEndpoint));
        collector.checkThat(jiraApi.getBulkCreateEndpoint(), equalTo(expectedBulkCreateEndpoint));
        collector.checkThat(jiraApi.getLinkPrefix(), equalTo(expectedLinkPrefix));
    }

    @Test
    public void shouldCreateCorrectClient() throws NoSuchAlgorithmException {
        assertThat(JIRA_HTTP_CLIENT.connectTimeout(), equalTo(Optional.empty()));
        assertThat(JIRA_HTTP_CLIENT.authenticator(), equalTo(Optional.empty()));
        assertThat(JIRA_HTTP_CLIENT.cookieHandler(), equalTo(Optional.empty()));
        assertThat(JIRA_HTTP_CLIENT.executor(), equalTo(Optional.empty()));
        assertThat(JIRA_HTTP_CLIENT.proxy(), equalTo(Optional.empty()));
        assertThat(JIRA_HTTP_CLIENT.followRedirects(), equalTo(NORMAL));
        assertThat(JIRA_HTTP_CLIENT.sslContext(), equalTo(SSLContext.getDefault()));
        assertThat(JIRA_HTTP_CLIENT.version(), equalTo(HttpClient.Version.HTTP_2));
    }
}
