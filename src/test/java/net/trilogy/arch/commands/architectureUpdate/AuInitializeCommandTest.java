package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.CommandTestBase;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static net.trilogy.arch.TestHelper.execute;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_API_SETTINGS_FILE_PATH;
import static net.trilogy.arch.commands.architectureUpdate.AuCommand.ARCHITECTURE_UPDATES_ROOT_FOLDER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuInitializeCommandTest extends CommandTestBase {
    @Test
    public void shouldUseCorrectAuFolder() {
        collector.checkThat(ARCHITECTURE_UPDATES_ROOT_FOLDER, equalTo("architecture-updates"));
    }

    @Test
    public void shouldExitWithHappyStatus() throws Exception {
        collector.checkThat(
                execute("au", "init", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0));
        collector.checkThat(
                execute("architecture-update", "init", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0));
        collector.checkThat(
                execute("au", "initialize", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0));
        collector.checkThat(
                execute("architecture-update", "initialize", "-c c", "-p p", "-s s", str(getTempDirectory())),
                equalTo(0));
    }

    @Test
    public void shouldCreateAuDirectory() throws Exception {
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder does not exist. (Precondition check)",
                exists(tempDirPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(false));

        final var code = execute("au", "init", "-c c", "-p p", "-s s", str(tempDirPath));

        collector.checkThat(code, is(equalTo(0)));

        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder was created.",
                isDirectory(tempDirPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(true));
    }

    @Test
    public void shouldNotifyIfArchitectureUpdatesDirectoryAlreadyExists() throws Exception {
        // Given
        Path tempDirPath = getTempDirectory();
        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder does not exist. (Precondition check)",
                exists(tempDirPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(false)
        );

        final var baseAuFolder = tempDirPath.resolve("architecture-updates");
        createDirectory(baseAuFolder);

        // When
        final var code = execute("au", "init", "-c c", "-p p", "-s s", str(tempDirPath));

        // Then
        collector.checkThat(code, equalTo(0));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
        collector.checkThat(dummyOut.getLog(), containsString("already exists"));
    }

    @Test
    public void shouldCreateGoogleCredentialsDirectory() throws Exception {
        final var tempDirPath = getTempDirectory();
        collector.checkThat(
                GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH + " folder does not exist. (Precondition check)",
                exists(tempDirPath.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH)),
                is(false));

        final var code = execute("au", "init", "-c c", "-p p", "-s s", str(tempDirPath));

        collector.checkThat(code, is(equalTo(0)));

        collector.checkThat(
                GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH + " folder was created.",
                isDirectory(tempDirPath.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH)),
                is(true));
    }

    @Test
    public void shouldCreateJiraSettingsFile() throws Exception {
        final var tempDirPath = getTempDirectory();
        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file does not exist. (Precondition check)",
                exists(tempDirPath.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                is(false));

        final int code = execute("architecture-update", "initialize", "--client-id", "c", "--project-id", "p", "--secret", "s", str(tempDirPath));

        collector.checkThat(code, is(equalTo(0)));

        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file was created.",
                exists(tempDirPath.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                is(true)
        );

        final String expectedJiraSettingsJson = "{\n" +
                "    \"base_uri\": \"https://jira.devfactory.com\",\n" +
                "    \"link_prefix\": \"/browse/\",\n" +
                "    \"get_story_endpoint\": \"/rest/api/2/issue/\",\n" +
                "    \"bulk_create_endpoint\": \"/rest/api/2/issue/bulk\"\n" +
                "}";
        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file contents are correct.",
                Files.readString(tempDirPath.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                equalTo(expectedJiraSettingsJson));
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void shouldNotOverwriteExistingJiraSettingsFile() throws Exception {
        // GIVEN:
        final var root = getTempDirectory();
        root.resolve(JIRA_API_SETTINGS_FILE_PATH).toFile().getParentFile().mkdirs();
        Files.writeString(root.resolve(JIRA_API_SETTINGS_FILE_PATH), "EXISTING CONTENTS");

        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file contains some existing contents. (Precondition check)",
                Files.readString(root.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                equalTo("EXISTING CONTENTS")
        );

        // WHEN:
        final int code = execute("architecture-update", "initialize", "--client-id", "c", "--project-id", "p", "--secret", "s", str(root));

        // THEN:
        collector.checkThat(code, not(equalTo(0)));
        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " file was not overridden.",
                Files.readString(root.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                equalTo("EXISTING CONTENTS"));
    }

    @Test
    public void shouldFailIfCreatingJiraSettingsFileFails() throws Exception {
        // GIVEN:
        final var rootDir = getTempDirectory();

        collector.checkThat(
                JIRA_API_SETTINGS_FILE_PATH + " folder does not exist. (Precondition check)",
                exists(rootDir.resolve(JIRA_API_SETTINGS_FILE_PATH)),
                is(false));

        var mockedFilesFacade = mock(FilesFacade.class);
        when(mockedFilesFacade.writeString(ArgumentMatchers.any(), ArgumentMatchers.contains("jira")))
                .thenThrow(new IOException("Something horrible has happened. Maybe we ran out of bytes."));

        final var app = Application.builder()
                .filesFacade(mockedFilesFacade)
                .build();

        // WHEN:
        final int status = execute(app, "au init -c c -p p -s s " + str(rootDir));

        // THEN:
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldFailIfCreatingGoogleCredentialsFileFails() throws Exception {
        // Given
        final var tempDirPath = getTempDirectory();

        collector.checkThat(
                GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH + " folder does not exist. (Precondition check)",
                exists(tempDirPath.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH)),
                is(false));

        final var mockedFilesFacade = mock(FilesFacade.class);
        when(mockedFilesFacade.writeString(ArgumentMatchers.any(), ArgumentMatchers.contains("google")))
                .thenThrow(new IOException("Something horrible has happened. Maybe we ran out of bytes."));
        final var app = Application.builder()
                .filesFacade(mockedFilesFacade)
                .build();

        // when
        final var status = execute(app, "au init -c c -p p -s s " + str(tempDirPath));

        // then
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldFailIfAuDirectoryAlreadyExists() throws Exception {
        final var rootDir = getTempDirectory();

        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));

        Files.writeString(auPathFrom(rootDir), "EXISTING CONTENTS");

        collector.checkThat(
                "Precondition check: AU must contain our contents.",
                Files.readString(auPathFrom(rootDir)),
                equalTo("EXISTING CONTENTS"));

        final var result = execute("au", "init", str(rootDir));

        collector.checkThat(
                result,
                not(equalTo(0)));
        collector.checkThat(
                Files.readString(auPathFrom(rootDir)),
                equalTo("EXISTING CONTENTS"));
    }

    @Test
    public void shouldRequireDocumentRootParameter() {
        collector.checkThat(
                execute("au", "init"),
                is(equalTo(2)));
    }

    @Test
    public void shouldCreateGoogleApiClientCredentialsFile() throws Exception {
        final var rootDir = getTempDirectory();
        final var clientId = "id";
        final var projectId = "proj";
        final var secret = "secret";

        execute("au", "init", str(rootDir), "-c " + clientId, "-p " + projectId, "-s " + secret);

        final var auCredFile = rootDir.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).resolve(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME);
        final var expected = "{\n" +
                "  \"installed\": {\n" +
                "    \"client_id\": \"" + clientId + "\",\n" +
                "    \"project_id\": \"" + projectId + "\",\n" +
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
        final var rootDir = getTempDirectory();

        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));

        final var auClientCredentialsFile = rootDir.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).resolve(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME);
        Files.writeString(auClientCredentialsFile.toAbsolutePath(), "EXISTING CONTENTS");

        collector.checkThat(
                "Precondition check: AU must contain our contents.",
                Files.readString(auClientCredentialsFile),
                equalTo("EXISTING CONTENTS"));

        final var code = execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));

        collector.checkThat(
                code,
                not(equalTo(0)));
        collector.checkThat(
                Files.readString(auClientCredentialsFile),
                equalTo("EXISTING CONTENTS"));
    }

    private static Path auPathFrom(Path rootPath) {
        return rootPath.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER).resolve("name").toAbsolutePath();
    }

    private static String str(Path tempDirPath) {
        return tempDirPath.toAbsolutePath().toString();
    }

    private static Path getTempDirectory() throws IOException {
        return createTempDirectory("arch-as-code_architecture-update_command_tests");
    }
}
