package net.trilogy.arch.adapter.jira;

import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.nio.file.Path;

import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_API_SETTINGS_FILE_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JiraApiFactoryTest {
    private static final String expectedBaseUri = "BASE-URI/";
    private static final String expectedGetStoryEndpoint = "GET-STORY-ENDPOINT/";
    private static final String expectedBulkCreateEndpoint = "BULK-CREATE-ENDPOINT/";
    private static final String expectedLinkPrefix = "LINK-PREFIX/";
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Before
    public void setUp() throws Exception {
        final var rootDir = Path.of("a", "random", "root", "directory");
        final var json = "" +
                "{\n" +
                "    \"base_uri\": \"" + expectedBaseUri + "\"\n" +
                "}";
        final var mockedFiles = mock(FilesFacade.class);

        when(mockedFiles.readString(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH)))
                .thenReturn(json);
    }

    @Test
    public void shouldUseTheRightConstants() {
        assertThat(JIRA_API_SETTINGS_FILE_PATH, equalTo(".arch-as-code/jira/settings.json"));
    }
}
