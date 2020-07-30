package net.trilogy.arch.e2e.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.adapter.jira.JiraApiFactory;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.ArgumentMatchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.TestHelper.execute;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_API_SETTINGS_FILE_PATH;
import static net.trilogy.arch.commands.architectureUpdate.AuCommand.ARCHITECTURE_UPDATES_ROOT_FOLDER;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuInitializeCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private final String client_id = "id";
    private final String project_id = "proj";
    private final String secret = "secret";

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void shouldUseCorrectAuFolder() {
        collector.checkThat(ARCHITECTURE_UPDATES_ROOT_FOLDER, equalTo("architecture-updates"));
    }

    @Test
    public void shouldExitWithHappyStatus() throws Exception {
        collector.checkThat(
                execute("au", "init", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0)
        );
        collector.checkThat(
                execute("architecture-update", "init", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0)
        );
        collector.checkThat(
                execute("au", "initialize", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0)
        );
        collector.checkThat(
                execute("architecture-update", "initialize", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0)
        );
    }

    @Test
    public void shouldCreateAuDirectory() throws Exception {
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder does not exist. (Precondition check)",
                Files.exists(tempDirPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(false)
        );

        Integer status = execute("au", "init", "-c c", "-p p", "-s s", str(tempDirPath));

        collector.checkThat(status, is(equalTo(0)));

        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder was created.",
                Files.isDirectory(tempDirPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(true)
        );
    }

    @Test
    public void shouldNotifyIfArchitectureUpdatesDirectoryAlreadyExists() throws Exception {
        // Given
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder does not exist. (Precondition check)",
                Files.exists(tempDirPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(false)
        );

        Path baseAuFolder = tempDirPath.resolve("architecture-updates");
        Files.createDirectory(baseAuFolder);

        // When
        Integer status = execute("au", "init", "-c c", "-p p", "-s s", str(tempDirPath));

        // Then
        collector.checkThat(status, equalTo(0));
        collector.checkThat(err.toString(), equalTo(""));
        collector.checkThat(out.toString(), containsString("already exists"));
    }

    @Test
    public void shouldCreateGoogleCredentialsDirectory() throws Exception {
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH + " folder does not exist. (Precondition check)",
                Files.exists(tempDirPath.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH)),
                is(false)
        );

        Integer status = execute("au", "init", "-c c", "-p p", "-s s", str(tempDirPath));

        collector.checkThat(status, is(equalTo(0)));

        collector.checkThat(
                GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH + " folder was created.",
                Files.isDirectory(tempDirPath.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH)),
                is(true)
        );
    }

    @Test
    public void shouldCreateJiraSettingsFile() throws Exception {
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file does not exist. (Precondition check)",
                Files.exists(tempDirPath.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                is(false)
        );

        int status = execute("architecture-update", "initialize", "--client-id", "c", "--project-id", "p", "--secret", "s", str(tempDirPath));

        collector.checkThat(status, is(equalTo(0)));

        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file was created.",
                Files.exists(tempDirPath.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                is(true)
        );

        final String expectedJiraSettingsJson = "{\n" +
                "    \"base_uri\": \"http://jira.devfactory.com\",\n" +
                "    \"link_prefix\": \"/browse/\",\n" +
                "    \"get_story_endpoint\": \"/rest/api/2/issue/\",\n" +
                "    \"bulk_create_endpoint\": \"/rest/api/2/issue/bulk\"\n" +
                "}";
        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file contents are correct.",
                Files.readString(tempDirPath.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                equalTo(expectedJiraSettingsJson)
        );
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void shouldNotOverwriteExistingJiraSettingsFile() throws Exception {
        // GIVEN:
        Path root = getTempDirectory();
        root.resolve(JIRA_API_SETTINGS_FILE_PATH).toFile().getParentFile().mkdirs();
        Files.writeString(root.resolve(JIRA_API_SETTINGS_FILE_PATH), "EXISTING CONTENTS");
        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file contains some existing contents. (Precondition check)",
                Files.readString(root.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                equalTo("EXISTING CONTENTS")
        );

        // WHEN:
        int status = execute("architecture-update", "initialize", "--client-id", "c", "--project-id", "p", "--secret", "s", str(root));

        // THEN:
        collector.checkThat(status, not(equalTo(0)));

        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file was not overridden.",
                Files.readString(root.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                equalTo("EXISTING CONTENTS")
        );
    }

    @Test
    public void shouldFailIfCreatingJiraSettingsFileFails() throws Exception {
        // GIVEN:
        Path rootDir = getTempDirectory();

        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " folder does not exist. (Precondition check)",
                Files.exists(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                is(false)
        );

        var mockedFilesFacade = mock(FilesFacade.class);
        when(
                mockedFilesFacade.writeString(ArgumentMatchers.any(), ArgumentMatchers.contains("jira"))
        ).thenThrow(
                new IOException("Something horrible has happened. Maybe we ran out of bytes.")
        );

        var app = Application.builder()
                .jiraApiFactory(mock(JiraApiFactory.class))
                .filesFacade(mockedFilesFacade)
                .build();

        // WHEN:
        int status = execute(app, "au init -c c -p p -s s " + str(rootDir));

        // THEN:
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldFailIfCreatingGoogleCredentialsFileFails() throws Exception {
        // Given
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH + " folder does not exist. (Precondition check)",
                Files.exists(tempDirPath.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH)),
                is(false)
        );
        var mockedFilesFacade = mock(FilesFacade.class);
        when(
                mockedFilesFacade.writeString(ArgumentMatchers.any(), ArgumentMatchers.contains("google"))
        ).thenThrow(
                new IOException("Something horrible has happened. Maybe we ran out of bytes.")
        );
        var app = Application.builder().jiraApiFactory(mock(JiraApiFactory.class)).filesFacade(mockedFilesFacade).build();

        // when
        Integer status = execute(app, "au init -c c -p p -s s " + str(tempDirPath));

        // then
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldFailIfAuDirectoryAlreadyExists() throws Exception {
        Path rootDir = getTempDirectory();
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));
        Files.writeString(auPathFrom(rootDir, "name"), "EXISTING CONTENTS");
        collector.checkThat(
                "Precondition check: AU must contain our contents.",
                Files.readString(auPathFrom(rootDir, "name")),
                equalTo("EXISTING CONTENTS")
        );

        Integer result = execute("au", "init", str(rootDir));

        collector.checkThat(
                result,
                not(equalTo(0))
        );
        collector.checkThat(
                Files.readString(auPathFrom(rootDir, "name")),
                equalTo("EXISTING CONTENTS")
        );
    }

    @Test
    public void shouldRequireDocumentRootParameter() throws Exception {
        collector.checkThat(
                execute("au", "init"),
                is(equalTo(2))
        );
    }

    @Test
    public void shouldCreateGoogleApiClientCredentialsFile() throws Exception {
        Path rootDir = getTempDirectory();
        execute("au", "init", str(rootDir), "-c " + client_id, "-p " + project_id, "-s " + secret);

        Path auCredFile = rootDir.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).resolve(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME);
        String expected = "{\n" +
                "  \"installed\": {\n" +
                "    \"client_id\": \"" + client_id + "\",\n" +
                "    \"project_id\": \"" + project_id + "\",\n" +
                "    \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "    \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "    \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "    \"client_secret\": \"" + secret + "\",\n" +
                "    \"redirect_uris\": [\n" +
                "      \"urn:ietf:wg:oauth:2.0:oob\",\n" +
                "      \"http://localhost\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        collector.checkThat(
                Files.readString(auCredFile),
                equalTo(expected));
    }

    @Test
    public void shouldNotOverrideExistingGoogleApiCredentialsFolder() throws Exception {
        Path rootDir = getTempDirectory();
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));
        final Path auClientCredentialsFile = rootDir.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).resolve(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME);
        Files.writeString(auClientCredentialsFile.toAbsolutePath(), "EXISTING CONTENTS");

        collector.checkThat(
                "Precondition check: AU must contain our contents.",
                Files.readString(auClientCredentialsFile),
                equalTo("EXISTING CONTENTS")
        );

        Integer result = execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));

        collector.checkThat(
                result,
                not(equalTo(0))
        );
        collector.checkThat(
                Files.readString(auClientCredentialsFile),
                equalTo("EXISTING CONTENTS")
        );
    }

    private Path auPathFrom(Path rootPath, String auName) {
        return rootPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER).resolve(auName).toAbsolutePath();
    }

    private String str(Path tempDirPath) {
        return tempDirPath.toAbsolutePath().toString();
    }

    private Path getTempDirectory() throws IOException {
        return Files.createTempDirectory("arch-as-code_architecture-update_command_tests");
    }
}
