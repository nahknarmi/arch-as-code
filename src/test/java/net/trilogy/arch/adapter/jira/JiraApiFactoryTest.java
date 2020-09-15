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

import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_API_SETTINGS_FILE_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraApiFactoryTest {
    private static final String expectedBaseUri = "BASE-URI/";
    private static final String expectedGetStoryEndpoint = "GET-STORY-ENDPOINT/";
    private static final String expectedBulkCreateEndpoint = "BULK-CREATE-ENDPOINT/";
    private static final String expectedLinkPrefix = "LINK-PREFIX/";
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
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
        HttpClient client = factory.createClient();
        JiraApi jiraApi = factory.create(mockedFiles, rootDir);

        collector.checkThat(jiraApi.getHttpClient(), is(client));
        collector.checkThat(jiraApi.getBaseUri(), equalTo(expectedBaseUri));
        collector.checkThat(jiraApi.getGetStoryEndpoint(), equalTo(expectedGetStoryEndpoint));
        collector.checkThat(jiraApi.getBulkCreateEndpoint(), equalTo(expectedBulkCreateEndpoint));
        collector.checkThat(jiraApi.getLinkPrefix(), equalTo(expectedLinkPrefix));
    }

    @Test
    public void shouldCreateCorrectClient() throws NoSuchAlgorithmException, IOException {

        final JiraApiFactory factory = new JiraApiFactory();

        HttpClient client = factory.createClient();

        assertThat(client.connectTimeout(), equalTo(Optional.empty()));
        assertThat(client.authenticator(), equalTo(Optional.empty()));
        assertThat(client.cookieHandler(), equalTo(Optional.empty()));
        assertThat(client.executor(), equalTo(Optional.empty()));
        assertThat(client.proxy(), equalTo(Optional.empty()));
        assertThat(client.followRedirects(), equalTo(HttpClient.Redirect.NORMAL));
        assertThat(client.sslContext(), equalTo(SSLContext.getDefault()));
        assertThat(client.version(), equalTo(HttpClient.Version.HTTP_2));
    }
}
