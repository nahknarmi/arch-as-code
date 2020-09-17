package net.trilogy.arch.e2e;

import net.trilogy.arch.Application;
import net.trilogy.arch.CommandTestBase;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;
import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ImportCommandE2ETest extends CommandTestBase {
    private Path tempProductDirectory;

    @Before
    public void setUp() throws Exception {
        tempProductDirectory = createTempDirectory("arch-as-code");
    }

    @After
    public void tearDown() throws Exception {
        //noinspection ResultOfMethodCallIgnored
        Files.walk(tempProductDirectory).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void shouldImportStructurizrJsonFile() throws Exception {
        // Given
        File workspacePath = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO).getPath());
        final String pathToSococo = workspacePath.getAbsolutePath();

        // When
        assertThat(execute("import", pathToSococo, tempProductDirectory.toAbsolutePath().toString()), equalTo(0));

        // Then
        File file = tempProductDirectory.resolve("product-architecture.yml").toFile();

        collector.checkThat(file.exists(), is(true));
        collector.checkThat(Files.readString(file.toPath()).contains("Sococo Import"), is(true));
    }

    @Test
    public void shouldImportStructurizrJsonFileWithMultipleSlashes() throws Exception {
        // Given
        File workspacePath = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_TEST_SPACES).getPath());
        final String pathToTestSpaces = workspacePath.getAbsolutePath();

        // When
        assertThat(execute("import", pathToTestSpaces, tempProductDirectory.toAbsolutePath().toString()), equalTo(0));

        // Then
        File file = tempProductDirectory.resolve("product-architecture.yml").toFile();

        collector.checkThat(file.exists(), is(true));
        collector.checkThat(Files.readString(file.toPath()).contains("TestSpaces is a tool!"), is(true));
    }

    @Test
    public void shouldGracefullyReportIOExceoptions() throws Exception {
        // Given
        File workspacePath = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_BIG_BANK).getPath());

        final FilesFacade mockedFilesFacade = Mockito.mock(FilesFacade.class);
        when(mockedFilesFacade.writeString(any(), any())).thenThrow(new IOException("Ran out of bytes!"));
        final Application app = Application.builder()
                .filesFacade(mockedFilesFacade)
                .build();

        // When
        final Integer statusCode = execute(app, "import", workspacePath.getAbsolutePath(), tempProductDirectory.toAbsolutePath().toString());

        // Then
        collector.checkThat(statusCode, not(0));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString("Failed to import\nError: java.io.IOException: Ran out of bytes!"));
    }

    @Test
    public void shouldGracefullyLogDocumnetationImageWriteErrors() throws Exception {
        // Given
        final FilesFacade mockedFilesFacade = Mockito.mock(FilesFacade.class);
        FileOutputStream mockedFileOutputStream = Mockito.mock(FileOutputStream.class);
        doThrow(new IOException("Boom!")).when(mockedFileOutputStream).write(any(byte[].class));
        when(mockedFilesFacade.newFileOutputStream(any())).thenReturn(mockedFileOutputStream);

        File workspacePath = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_EMBEDDED_IMAGE).getPath());

        final Application app = Application.builder()
                .filesFacade(mockedFilesFacade)
                .build();

        // When
        Integer status = execute(app, "import", workspacePath.getAbsolutePath(), tempProductDirectory.toAbsolutePath().toString());

        collector.checkThat(status, not(0));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString("Failed to import\nError: java.io.IOException: Boom!"));
    }
}
