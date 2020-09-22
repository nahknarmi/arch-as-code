package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.CommandTestBase;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate.ARCHITECTURE_UPDATE_YML;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuAnnotateCommandTest extends CommandTestBase {
    Path rootPath;
    Path originalAuWithComponentsDirectoryPath;
    Path originalAuWithoutComponentsDirectoryPath;
    Path originalAuWithTddContentsDirectoryPath;
    Path changedAuWithComponentsDirectoryPath;
    Path changedAuWithoutComponentsDirectoryPath;
    Path changedAuWithTddContentsDirectoryPath;

    private static String toString(Path path) {
        return path.toAbsolutePath().toString();
    }

    @Before
    public void setUp() throws Exception {
        rootPath = TestHelper.getPath(getClass(), TestHelper.ROOT_PATH_TO_TEST_AU_ANNOTATE);
        originalAuWithComponentsDirectoryPath = rootPath.resolve("architecture-updates/validWithComponents/");
        originalAuWithoutComponentsDirectoryPath = rootPath.resolve("architecture-updates/validWithoutComponents/");
        originalAuWithTddContentsDirectoryPath = rootPath.resolve("architecture-updates/validWithComponentsAndTddContentFiles/");

        changedAuWithComponentsDirectoryPath = rootPath.resolve("architecture-updates/validWithComponentsClone/");
        changedAuWithoutComponentsDirectoryPath = rootPath.resolve("architecture-updates/validWithoutComponentsClone/");
        changedAuWithTddContentsDirectoryPath = rootPath.resolve("architecture-updates/validWithComponentsAndTddContentFilesClone/");

        copyDirectory(originalAuWithComponentsDirectoryPath.toFile(), changedAuWithComponentsDirectoryPath.toFile());
        copyDirectory(originalAuWithoutComponentsDirectoryPath.toFile(), changedAuWithoutComponentsDirectoryPath.toFile());
        copyDirectory(originalAuWithTddContentsDirectoryPath.toFile(), changedAuWithTddContentsDirectoryPath.toFile());
    }

    @After
    public void tearDown() throws Exception {
        deleteDirectory(changedAuWithComponentsDirectoryPath.toFile());
        deleteDirectory(changedAuWithoutComponentsDirectoryPath.toFile());
        deleteDirectory(changedAuWithTddContentsDirectoryPath.toFile());
    }

    @Test
    public void shouldAnnotateAuWithPath() throws Exception {
        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithComponentsDirectoryPath), toString(rootPath));

        var actual = Files.readString(changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        // THEN
        var expected = Files.readString(originalAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replaceFirst("component-id: \"31\"",
                        "component-id: \"31\"\n  component-path: c4://Internet Banking System/API Application/Reset Password Controller")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"\n  component-path: c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: \"34\"",
                        "component-id: \"34\"\n  component-path: c4://Internet Banking System/API Application/E-mail Component");

        collector.checkThat(dummyOut.getLog(), equalTo("AU has been annotated.\n"));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(0));
        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldAnnotateAuWithTddContentFile() throws Exception {
        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithTddContentsDirectoryPath), toString(rootPath));

        var actual = Files.readString(changedAuWithTddContentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        // THEN
        var expected = Files.readString(originalAuWithTddContentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replaceFirst("component-id: \"31\"",
                        "component-id: \"31\"\n  component-path: c4://Internet Banking System/API Application/Reset Password Controller")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"\n  component-path: c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: \"34\"",
                        "component-id: \"34\"\n  component-path: c4://Internet Banking System/API Application/E-mail Component")
                .replaceFirst("TDD 1.1:\n\\s*file: null",
                        "TDD 1.1:\n      file: 'TDD 1.1 : Component-31.txt'")
                .replaceFirst("TDD 1.2:\n\\s*file: null",
                        "TDD 1.2:\n      file: 'TDD 1.2 : Component-31.txt'")
                .replaceFirst("TDD 2.1:\n\\s*file: null",
                        "TDD 2.1:\n      file: 'TDD 2.1 : Component-30.txt'");

        collector.checkThat(dummyOut.getLog(), equalTo("AU has been annotated.\n"));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(0));
        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldRefreshAnnotations() throws Exception {
        // GIVEN
        TestHelper.execute("au", "annotate", toString(changedAuWithComponentsDirectoryPath), toString(rootPath));

        Path writeDestination = changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML);
        String writeSource = Files.readString(changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replace("component-id: \"31\"", "component-id: \"29\"");
        Files.writeString(writeDestination, writeSource);

        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithComponentsDirectoryPath), toString(rootPath));

        var actual = Files.readString(changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        // THEN
        var expected = Files.readString(originalAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replaceFirst("component-id: \"31\"",
                        "component-id: \"29\"\n  component-path: c4://Internet Banking System/API Application/Sign In Controller")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"\n  component-path: c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: \"34\"",
                        "component-id: \"34\"\n  component-path: c4://Internet Banking System/API Application/E-mail Component");

        collector.checkThat(dummyErr.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(0));
        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldHandleNoComponents() throws Exception {
        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithoutComponentsDirectoryPath), toString(rootPath));

        var actual = Files.readString(changedAuWithoutComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        // THEN
        var expected = Files.readString(originalAuWithoutComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        collector.checkThat(actual, equalTo(expected));
        collector.checkThat(dummyErr.getLog(), equalTo("No valid components to annotate.\n"));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(1));
    }

    @Test
    public void shouldHandleWithOnlyInvalidComponents() throws Exception {
        // GIVEN
        Path writeDestination = changedAuWithoutComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML);
        String writeSource = Files.readString(changedAuWithoutComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replace("component-id: '[SAMPLE-COMPONENT-ID]'", "component-id: \"404\"");
        Files.writeString(writeDestination, writeSource);

        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithoutComponentsDirectoryPath), toString(rootPath));

        var actual = Files.readString(changedAuWithoutComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        // THEN
        var expected = Files.readString(originalAuWithoutComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replace("component-id: '[SAMPLE-COMPONENT-ID]'", "component-id: \"404\"");

        collector.checkThat(actual, equalTo(expected));
        collector.checkThat(dummyErr.getLog(), equalTo("No valid components to annotate.\n"));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(1));
    }

    @Test
    public void shouldIgnoreInvalidComponentsAmongstValid() throws Exception {
        // GIVEN
        Path writeDestination = changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML);
        String writeSource = Files.readString(changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replace("component-id: \"34\"", "component-id: \"404\"");
        Files.writeString(writeDestination, writeSource);

        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithComponentsDirectoryPath), toString(rootPath));

        var actual = Files.readString(changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML));

        // THEN
        var expected = Files.readString(originalAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML))
                .replaceFirst("component-id: \"34\"", "component-id: \"404\"")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"\n  component-path: c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: \"31\"",
                        "component-id: \"31\"\n  component-path: c4://Internet Banking System/API Application/Reset Password Controller");

        collector.checkThat(dummyOut.getLog(), equalTo("AU has been annotated.\n"));
        collector.checkThat(actual, equalTo(expected));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(0));
    }

    @Test
    public void shouldNotifyUserWhenAUFailsToLoad() throws Exception {
        // GIVEN
        final FilesFacade mockedFilesFacade = spy(FilesFacade.class);
        when(mockedFilesFacade.readString(changedAuWithComponentsDirectoryPath.resolve(ARCHITECTURE_UPDATE_YML)))
                .thenThrow(new IOException("error-message", new RuntimeException("Boom!")));

        // WHEN
        Application app = Application.builder().filesFacade(mockedFilesFacade).build();
        int status = TestHelper.execute(app, "au annotate " + toString(changedAuWithComponentsDirectoryPath) + " " + toString(rootPath));

        // THEN
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), equalTo("Unable to load Architecture Update.\nError: java.io.IOException: error-message\nCause: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, equalTo(2));
    }

    @Test
    public void shouldNotifyUserWhenArchitectureDatastructureFailsToLoad() throws Exception {
        // GIVEN
        final FilesFacade spyedFilesFacade = spy(new FilesFacade());
        final Path a = rootPath.resolve("product-architecture.yml");
        doThrow(new IOException("error-message", new RuntimeException("Boom!"))).when(spyedFilesFacade).readString(eq(a));

        // WHEN
        int status = TestHelper.execute(Application.builder().filesFacade(spyedFilesFacade).build(),
                "au annotate " + toString(changedAuWithComponentsDirectoryPath) + " " + toString(rootPath));

        // THEN
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), equalTo("Unable to load Architecture product-architecture.yml.\nError: java.io.IOException: error-message\nCause: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, equalTo(2));
    }

    @Test
    public void shouldNotifuUserWhenAnnotationFailsToWrite() throws Exception {
        // GIVEN
        final FilesFacade mockedFilesFacade = mock(FilesFacade.class);
        when(mockedFilesFacade.readString(any())).thenCallRealMethod();
        when(mockedFilesFacade.writeString(any(), any())).thenThrow(new IOException("Ran out of bytes!"));

        // WHEN
        int status = TestHelper.execute(Application.builder().filesFacade(mockedFilesFacade).build(),
                "au annotate " + toString(changedAuWithComponentsDirectoryPath) + " " + toString(rootPath));

        // THEN
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), equalTo("Unable to write annotated Architecture Update to yaml file.\nError: java.io.IOException: Ran out of bytes!\n"));
        collector.checkThat(status, equalTo(2));
    }
}
