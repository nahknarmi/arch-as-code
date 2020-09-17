package net.trilogy.arch.e2e;

import net.trilogy.arch.CommandTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.Files.deleteIfExists;
import static net.trilogy.arch.TestHelper.ROOT_PATH_TO_TEST_VALIDATION;
import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ListComponentsCommandE2ETest extends CommandTestBase {
    private File rootDir;

    @Before
    public void setUp() {
        rootDir = new File(getClass().getResource(ROOT_PATH_TO_TEST_VALIDATION).getPath());
    }

    private void initFileForTest(String fileName) throws IOException {
        Files.copy(rootDir.toPath().resolve(fileName), rootDir.toPath().resolve("product-architecture.yml"));
    }

    @After
    public void tearDown() throws IOException {
        deleteIfExists(rootDir.toPath().resolve("product-architecture.yml"));
    }

    @Test
    public void shouldOutputComponentsList() throws Exception {
        initFileForTest("allValidSchema.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), equalTo(
                "ID, Name, Path\n" +
                        "13, DevSpaces/DevSpaces API/Sign In Controller, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Sign In Controller\n" +
                        "14, DevSpaces/DevSpaces API/Security Component, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Security Component\n" +
                        "15, DevSpaces/DevSpaces API/Reset Password Controller, \n" +
                        "16, DevSpaces/DevSpaces API/E-mail Component, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-E-mail Component\n"
        ));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldOutputComponentsListFilteredBySearchString() throws Exception {
        initFileForTest("allValidSchema.yml");

        int status = execute("list-components", "-s password", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), equalTo(
                "ID, Name, Path\n" +
                        "14, DevSpaces/DevSpaces API/Security Component, c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Security Component\n" +
                        "15, DevSpaces/DevSpaces API/Reset Password Controller, \n"
        ));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldHandleEmptyModel() throws Exception {
        initFileForTest("emptyModel.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), equalTo("ID, Name, Path\n"));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldHandleMissingModelProperties() throws Exception {
        initFileForTest("missingModelProperties.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), equalTo("ID, Name, Path\n"));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldHandleMissingModel() throws Exception {
        initFileForTest("missingMetadata.yml");

        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(1));
        collector.checkThat(dummyErr.getLog(), containsString("Unable to load architecture"));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
    }

    @Test
    public void shouldFailIfArchitectureNotFound() {
        int status = execute("list-components", rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString("Unable to load architecture\nError: java.nio.file.NoSuchFileException"));
    }
}
