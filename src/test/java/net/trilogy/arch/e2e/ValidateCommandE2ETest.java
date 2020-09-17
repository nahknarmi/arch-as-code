package net.trilogy.arch.e2e;

import net.trilogy.arch.TestHelper;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.TestHelper.JSON_STRUCTURIZR_NO_SYSTEM;
import static net.trilogy.arch.TestHelper.execute;
import static net.trilogy.arch.TestHelper.getPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ValidateCommandE2ETest {
    @Test
    public void shouldBeOkayWithMostlyEmptyImportedJson() throws Exception {
        String jsonPath = getClass().getResource(JSON_STRUCTURIZR_NO_SYSTEM).getPath();
        Path root = Files.createTempDirectory("aac").toAbsolutePath();

        execute("import", jsonPath, root.toString());

        int status = execute("validate", root.toString());
        assertThat(status, equalTo(0));
    }

    @Test
    public void validate() {
        final var documentationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());

        final var statusCode = execute("validate " + documentationRoot.getAbsolutePath());

        assertThat(statusCode, equalTo(0));
    }

    @Test
    public void validate_missing_metadata() throws Exception {
        final Path rootDir = Files.createTempDirectory("aac");
        Files.copy(getPath(getClass(), TestHelper.ROOT_PATH_TO_TEST_VALIDATION).resolve("missingMetadata.yml"), rootDir.resolve("product-architecture.yml"));

        Integer statusCode = execute("validate " + rootDir.toAbsolutePath());

        assertThat(statusCode, not(equalTo(0)));
    }
}
