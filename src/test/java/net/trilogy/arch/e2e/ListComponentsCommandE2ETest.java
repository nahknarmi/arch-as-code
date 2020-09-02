package net.trilogy.arch.e2e;

import net.trilogy.arch.TestHelper;
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

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ListComponentsCommandE2ETest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private File rootDir;

    @Before
    public void setUp() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        rootDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_VALIDATION).getPath());
    }

    private void initFileForTest(String fileName) throws IOException {
        Files.copy(rootDir.toPath().resolve(fileName), rootDir.toPath().resolve("product-architecture.yml"));
    }

    @After
    public void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);

        Files.deleteIfExists(rootDir.toPath().resolve("product-architecture.yml"));
    }

    @Test
    public void shouldOutputComponentsList() throws Exception {
        initFileForTest("allValidSchema.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), equalTo(
                "ID, Name, Path\n" +
                        "13, DevSpaces/DevSpaces API/Sign In Controller, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Sign In Controller\n" +
                        "14, DevSpaces/DevSpaces API/Security Component, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Security Component\n" +
                        "15, DevSpaces/DevSpaces API/Reset Password Controller, \n" +
                        "16, DevSpaces/DevSpaces API/E-mail Component, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-E-mail Component\n"
        ));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldOutputComponentsListFilteredBySearchString() throws Exception {
        initFileForTest("allValidSchema.yml");

        int status = TestHelper.execute("list-components", "-s password", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), equalTo(
                "ID, Name, Path\n" +
                        "14, DevSpaces/DevSpaces API/Security Component, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Security Component\n" +
                        "15, DevSpaces/DevSpaces API/Reset Password Controller, \n"
        ));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldHandleEmptyModel() throws Exception {
        initFileForTest("emptyModel.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), equalTo("ID, Name, Path\n"));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldHandleMissingModelProperties() throws Exception {
        initFileForTest("missingModelProperties.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), equalTo("ID, Name, Path\n"));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldHandleMissingModel() throws Exception {
        initFileForTest("missingMetadata.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(1));
        collector.checkThat(err.toString(), containsString("Unable to load architecture"));
        collector.checkThat(out.toString(), equalTo(""));
    }

    @Test
    public void shouldFailIfArchitectureNotFound() {
        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("Unable to load architecture\nError: java.nio.file.NoSuchFileException"));
    }
}
