package net.trilogy.arch.e2e.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuAnnotateCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    Path rootPath;
    Path originalAuWithComponentsPath;
    Path changedAuWithComponentsPath;
    Path originalAuWithoutComponentsPath;
    Path changedAuWithoutComponentsPath;

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        rootPath = TestHelper.getPath(getClass(), TestHelper.ROOT_PATH_TO_TEST_AU_ANNOTATE);
        originalAuWithComponentsPath = rootPath.resolve("architecture-updates/valid-with-components.yml");
        originalAuWithoutComponentsPath = rootPath.resolve("architecture-updates/valid-without-components.yml");

        changedAuWithComponentsPath = rootPath.resolve("architecture-updates/valid-with-components-clone.yml");
        changedAuWithoutComponentsPath = rootPath.resolve("architecture-updates/valid-without-components-clone.yml");

        Files.copy(originalAuWithComponentsPath, changedAuWithComponentsPath);
        Files.copy(originalAuWithoutComponentsPath, changedAuWithoutComponentsPath);

        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(changedAuWithComponentsPath);
        Files.deleteIfExists(changedAuWithoutComponentsPath);

        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void shouldAnnotate() throws Exception {
        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithComponentsPath), toString(rootPath));

        var actual = Files.readString(changedAuWithComponentsPath);

        // THEN
        var expected = Files.readString(originalAuWithComponentsPath)
                .replaceFirst("component-id: '31'",
                        "component-id: '31'  # c4://Internet Banking System/API Application/Reset Password Controller")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"  # c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: '34'",
                        "component-id: '34'  # c4://Internet Banking System/API Application/E-mail Component");

        collector.checkThat(out.toString(), equalTo("AU has been annotated with component paths.\n"));
        collector.checkThat(err.toString(), equalTo(""));
        collector.checkThat(status, equalTo(0));
        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldRefreshAnnotations() throws Exception {
        // GIVEN
        TestHelper.execute("au", "annotate", toString(changedAuWithComponentsPath), toString(rootPath));

        Files.writeString(changedAuWithComponentsPath, Files.readString(changedAuWithComponentsPath).replace("component-id: '31'", "component-id: '29'"));

        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithComponentsPath), toString(rootPath));

        var actual = Files.readString(changedAuWithComponentsPath);

        // THEN
        var expected = Files.readString(originalAuWithComponentsPath)
                .replaceFirst("component-id: '31'",
                        "component-id: '29'  # c4://Internet Banking System/API Application/Sign In Controller")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"  # c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: '34'",
                        "component-id: '34'  # c4://Internet Banking System/API Application/E-mail Component");

        collector.checkThat(err.toString(), equalTo(""));
        collector.checkThat(status, equalTo(0));
        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldHandleNoComponents() throws Exception {
        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithoutComponentsPath), toString(rootPath));

        var actual = Files.readString(changedAuWithoutComponentsPath);

        // THEN
        var expected = Files.readString(originalAuWithoutComponentsPath);

        collector.checkThat(actual, equalTo(expected));
        collector.checkThat(err.toString(), equalTo("No valid components to annotate.\n"));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(status, equalTo(1));
    }

    @Test
    public void shouldHandleWithOnlyInvalidComponents() throws Exception {
        // GIVEN
        Files.writeString(changedAuWithoutComponentsPath, Files.readString(changedAuWithoutComponentsPath).replace("component-id: '[SAMPLE-COMPONENT-ID]'", "component-id: '404'"));

        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithoutComponentsPath), toString(rootPath));

        var actual = Files.readString(changedAuWithoutComponentsPath);

        // THEN
        var expected = Files.readString(originalAuWithoutComponentsPath).replace("component-id: '[SAMPLE-COMPONENT-ID]'", "component-id: '404'");

        collector.checkThat(actual, equalTo(expected));
        collector.checkThat(err.toString(), equalTo("No valid components to annotate.\n"));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(status, equalTo(1));
    }

    @Test
    public void shouldIgnoreInvalidComponentsAmongstValid() throws Exception {
        // GIVEN
        Files.writeString(changedAuWithComponentsPath, Files.readString(changedAuWithComponentsPath).replace("component-id: '34'", "component-id: '404'"));

        // WHEN
        int status = TestHelper.execute("au", "annotate", toString(changedAuWithComponentsPath), toString(rootPath));

        var actual = Files.readString(changedAuWithComponentsPath);

        // THEN
        var expected = Files.readString(originalAuWithComponentsPath)
                .replaceFirst("component-id: '34'", "component-id: '404'")
                .replaceFirst("component-id: \"30\"",
                        "component-id: \"30\"  # c4://Internet Banking System/API Application/Accounts Summary Controller")
                .replaceFirst("component-id: '31'",
                        "component-id: '31'  # c4://Internet Banking System/API Application/Reset Password Controller");

        collector.checkThat(out.toString(), equalTo("AU has been annotated with component paths.\n"));
        collector.checkThat(actual, equalTo(expected));
        collector.checkThat(err.toString(), equalTo(""));
        collector.checkThat(status, equalTo(0));
    }

    @Test
    public void shouldNotifyUserWhenAUFailsToLoad() throws Exception {
        // GIVEN
        final FilesFacade mockedFilesFacade = spy(FilesFacade.class);
        when(mockedFilesFacade.readString(changedAuWithComponentsPath)).thenThrow(new IOException("error-message", new RuntimeException("Boom!")));

        // WHEN
        int status = TestHelper.execute(Application.builder().filesFacade(mockedFilesFacade).build(),
                "au annotate " + toString(changedAuWithComponentsPath) + " " + toString(rootPath));

        // THEN
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to load Architecture Update.\nError: java.io.IOException: error-message\nCause: java.lang.RuntimeException: Boom!\n"));
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
                "au annotate " + toString(changedAuWithComponentsPath) + " " + toString(rootPath));

        // THEN
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to load Architecture product-architecture.yml.\nError thrown: java.io.IOException: error-message\nCause: java.lang.RuntimeException: Boom!\n"));
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
                "au annotate " + toString(changedAuWithComponentsPath) + " " + toString(rootPath));

        // THEN
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to write annotations to Architecture Update.\nError: java.io.IOException: Ran out of bytes!\nCause: null\n"));
        collector.checkThat(status, equalTo(2));
    }

    private String toString(Path path) {
        return path.toAbsolutePath().toString();
    }
}
