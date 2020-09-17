package net.trilogy.arch.commands.architectureUpdate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.trilogy.arch.Application;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.google.GoogleDocsFacade;
import net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory;
import net.trilogy.arch.adapter.jira.JiraApiFactory;
import net.trilogy.arch.facade.FilesFacade;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.setErr;
import static java.lang.System.setOut;
import static java.util.Objects.requireNonNull;
import static net.trilogy.arch.TestHelper.execute;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.commands.architectureUpdate.AuCommand.ARCHITECTURE_UPDATES_ROOT_FOLDER;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.ARCHITECTURE_UPDATE_YML;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.prefilledWithBlanks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AuNewCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private GoogleDocsFacade googleDocsApiMock;
    private FilesFacade filesFacadeSpy;
    private GitInterface gitInterfaceSpy;
    private Application app;
    private File rootDir;

    private static Path initializeAuDirectory(Path rootDir) {
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));

        return rootDir.resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER);
    }

    private static String str(Path tempDirPath) {
        return tempDirPath.toAbsolutePath().toString();
    }

    private static Path getTempRepositoryDirectory() throws Exception {
        var repoDir = Files.createTempDirectory("aac");
        var rootDir = Files.createDirectory(repoDir.resolve("root"));
        var git = Git.init().setDirectory(repoDir.toFile()).call();
        git.add().addFilepattern(".").call();
        git.commit().setMessage("First!").call();
        git.checkout().setCreateBranch(true).setName("au-name").call();
        return rootDir;
    }

    @Before
    public void setUp() throws Exception {
        out.reset();
        err.reset();
        setOut(new PrintStream(out));
        setErr(new PrintStream(err));

        rootDir = getTempRepositoryDirectory().toFile();
        googleDocsApiMock = mock(GoogleDocsFacade.class);
        final var googleDocsApiFactoryMock = mock(GoogleDocsAuthorizedApiFactory.class);
        when(googleDocsApiFactoryMock.getAuthorizedDocsApi(rootDir)).thenReturn(googleDocsApiMock);
        filesFacadeSpy = spy(new FilesFacade());
        gitInterfaceSpy = spy(new GitInterface());

        app = Application.builder()
                .googleDocsAuthorizedApiFactory(googleDocsApiFactoryMock)
                .filesFacade(filesFacadeSpy)
                .gitInterface(gitInterfaceSpy)
                .build();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(rootDir);
        setOut(originalOut);
        setErr(originalErr);
    }

    @Test
    public void shouldFailGracefullyIfGitApiFails() throws Exception {
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir.toPath()));
        out.reset();

        doThrow(new RuntimeException("Boom!")).when(gitInterfaceSpy).getBranch(any());

        int status = execute(app, "au new not-au-name " + str(rootDir.toPath()));

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("ERROR: Unable to check git branch\nError:"));
    }

    @Test
    public void shouldFailIfBranchNameDoesNotMatch() {
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir.toPath()));
        out.reset();

        final var status = execute("au", "new", "not-au-name", str(rootDir.toPath()));

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString(
                "ERROR: AU must be created in git branch of same name.\n" +
                        "Current git branch: 'au-name'\n" +
                        "Au name: 'not-au-name'\n"));
    }

    @Test
    public void shouldExitWithHappyStatusWithoutP1_short() {
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir.toPath()));

        collector.checkThat(
                execute("au", "new", "au-name", str(rootDir.toPath())),
                is(equalTo(0)));
    }

    @Test
    public void shouldExitWithHappyStatusWithoutP1_long() {
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir.toPath()));

        collector.checkThat(
                execute("architecture-update", "new", "au-name", str(rootDir.toPath())),
                is(equalTo(0)));
    }

    @Test
    public void shouldExitWithHappyStatusWithP1_short() throws Exception {
        mockGoogleDocsApi();
        initializeAuDirectory(rootDir.toPath());
        collector.checkThat(
                execute(app, "au new au-name -p url " + str(rootDir.toPath())),
                is(equalTo(0)));
    }

    @Test
    public void shouldExitWithHappyStatusWithP1_long() throws Exception {
        mockGoogleDocsApi();
        initializeAuDirectory(rootDir.toPath());
        collector.checkThat(
                execute(app, "architecture-update new au-name --p1-url url " + str(rootDir.toPath())),
                is(equalTo(0)));
    }

    @Test
    public void shouldFailGracefullyIfGoogleApiUninitialized() {
        final var status = execute("au", "new", "au-name", str(rootDir.toPath()), "-p", "p1GoogleDocUrl");

        final var auFile = rootDir.toPath().resolve("architecture-updates/au-name.yml");
        final var configPath = rootDir.toPath().resolve(".arch-as-code").toAbsolutePath().toString();

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(Files.exists(auFile), is(false));
        collector.checkThat(err.toString(), containsString("Unable to initialize Google Docs API. Does configuration " + configPath + " exist?\n"));
    }

    @Test
    public void shouldFailGracefullyIfFailsToCreateDirectory() throws Exception {
        final var auFolder = rootDir.toPath().resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER);

        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder does not exist. (Precondition check)",
                Files.exists(auFolder),
                is(false));

        doThrow(new IOException("details", new RuntimeException("cause"))).when(filesFacadeSpy).createDirectory(eq(auFolder));

        final var status = execute(app, "au new au-name " + str(rootDir.toPath()));

        final var auFile = rootDir.toPath().resolve("architecture-updates/au-name.yml");

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(Files.exists(auFile), is(false));
        collector.checkThat(err.toString(), containsString("Unable to create architecture-updates directory."));
        collector.checkThat(err.toString(), containsString("details"));
        collector.checkThat(err.toString(), containsString("cause"));
    }

    @Test
    public void shouldHandleCreatingFolderIfDoesNotExist() throws Exception {
        collector.checkThat(
                ARCHITECTURE_UPDATES_ROOT_FOLDER + " folder does not exist. (Precondition check)",
                Files.exists(rootDir.toPath().resolve(ARCHITECTURE_UPDATES_ROOT_FOLDER)),
                is(false));

        final var status = execute("au", "new", "au-name", str(rootDir.toPath()));

        final var auFile = rootDir.toPath().resolve("architecture-updates/au-name/architecture-update.yml");

        collector.checkThat(status, equalTo(0));
        collector.checkThat(Files.exists(auFile), is(true));
        collector.checkThat(
                Files.readString(auFile.toAbsolutePath()),
                equalTo(YAML_OBJECT_MAPPER.writeValueAsString(prefilledWithBlanks()
                        .name("au-name")
                        .build())));
    }

    @Test
    public void shouldCreateFileWithoutP1() throws Exception {
        // Given
        execute("init", str(rootDir.toPath()), "-i i", "-k k", "-s s");

        final var auName = "au-name";
        final var allAusDir = initializeAuDirectory(rootDir.toPath());
        final var currentAuDir = allAusDir.resolve(auName);
        final var auFile = currentAuDir.resolve(ARCHITECTURE_UPDATE_YML);
        collector.checkThat(
                "AU does not already exist. (Precondition check)",
                Files.exists(auFile),
                is(false));

        // When
        final var exitCode = execute("au", "new", auName, str(rootDir.toPath()));

        // Then
        collector.checkThat(exitCode, is(equalTo(0)));
        collector.checkThat(Files.exists(auFile), is(true));
        collector.checkThat(
                Files.readString(auFile.toAbsolutePath()),
                equalTo(YAML_OBJECT_MAPPER.writeValueAsString(
                        prefilledWithBlanks()
                                .name(auName)
                                .build())));
    }

    @Test
    public void shouldCreateFileWithP1() throws Exception {
        // GIVEN
        mockGoogleDocsApi();
        execute("init", str(rootDir.toPath()), "-i i", "-k k", "-s s");

        String auName = "au-name";
        Path allAusDir = initializeAuDirectory(rootDir.toPath());
        Path auFile = allAusDir.resolve(auName).resolve(ARCHITECTURE_UPDATE_YML);
        collector.checkThat(
                "AU does not already exist. (Precondition check)",
                Files.exists(auFile),
                is(false));

        // WHEN
        final var exitCode = execute(app, "au new " + auName + " -p url " + str(rootDir.toPath()));

        // THEN
        collector.checkThat(exitCode, is(equalTo(0)));
        collector.checkThat(Files.exists(auFile), is(true));
        collector.checkThat(
                Files.readString(auFile.toAbsolutePath()),
                containsString("ABCD-1231"));
    }

    @Test
    public void shouldNotCreateFileIfAlreadyExists() throws Exception {
        final var rootDir = getTempRepositoryDirectory();
        final var auName = "au-name";
        final var allAusDir = initializeAuDirectory(rootDir);
        final var auFile = allAusDir.resolve(auName).resolve(ARCHITECTURE_UPDATE_YML);

        final var setupStatus = execute("au", "new", auName, str(rootDir));
        Files.writeString(auFile, "EXISTING CONTENTS");

        collector.checkThat(setupStatus, equalTo(0));
        collector.checkThat(err.toString(), equalTo(""));

        collector.checkThat(
                "Precondition check: AU must contain our contents.",
                Files.readString(auFile),
                equalTo("EXISTING CONTENTS"));

        collector.checkThat(
                "Overwriting an AU must exit with failed status.",
                execute("au", "new", auName, str(rootDir)),
                not(equalTo(0)));

        collector.checkThat(
                err.toString(),
                containsString("/root/architecture-updates/au-name/architecture-update.yml already exists. Try a different name."));

        collector.checkThat(
                "Must not overwrite an AU",
                Files.readString(auFile),
                equalTo("EXISTING CONTENTS"));
    }

    @Test
    public void shouldFailIfCannotWriteFile() throws Exception {
        final var rootDir = getTempRepositoryDirectory();
        execute("au", "init", "-c c", "-p p", "-s s", str(rootDir));

        final var auName = "au-name";

        final var mockedFilesFacade = mock(FilesFacade.class);

        when(mockedFilesFacade.writeString(any(), any()))
                .thenThrow(new IOException("No disk space!"));

        final var app = Application.builder()
                .filesFacade(mockedFilesFacade)
                .build();
        final var command = "au new " + auName + " " + str(rootDir);

        assertThat(execute(app, command), not(equalTo(0)));
        collector.checkThat(err.toString(), containsString("Unable to write AU file."));
        collector.checkThat(err.toString(), containsString("No disk space!"));
    }

    private void mockGoogleDocsApi() throws IOException {
        final var rawFileContents = Files.readString(Paths.get(requireNonNull(getClass().getClassLoader().getResource(TestHelper.ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json")).getPath()));
        final var jsonFileContents = new ObjectMapper().readValue(rawFileContents, JsonNode.class);
        when(googleDocsApiMock.fetch("url")).thenReturn(new GoogleDocsFacade.Response(jsonFileContents, null));
    }
}
