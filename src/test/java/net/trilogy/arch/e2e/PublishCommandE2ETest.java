package net.trilogy.arch.e2e;

import net.trilogy.arch.Application;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class PublishCommandE2ETest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

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
    public void shouldSuccessfullyPublish() {
        // Given
        StructurizrAdapter structurizrAdapter = spy(StructurizrAdapter.class);
        Application app = Application.builder()
                .structurizrAdapter(structurizrAdapter)
                .build();
        Path rootDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath()).toPath();

        // When
        Integer statusCode = execute(app, "publish", rootDir.toString());

        // Then
        verify(structurizrAdapter, times(1)).publish(any());
        collector.checkThat(statusCode, equalTo(0));
        collector.checkThat(out.toString(), equalTo("Successfully published to Structurizr!\n"));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldListValidationsErrorsWhenProductArchitectureInvalid() throws Exception {
        // Given
        StructurizrAdapter structurizrAdapter = spy(StructurizrAdapter.class);
        Application app = Application.builder()
                .structurizrAdapter(structurizrAdapter)
                .build();
        Path rootDir = Files.createTempDirectory("aac");
        Path productArch = Files.createFile(rootDir.resolve("product-architecture.yml"));
        Files.write(
                productArch,
                Files.readAllBytes(Path.of(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_VALIDATION).getPath()).resolve("missingMetadata.yml"))
        );

        // When
        Integer statusCode = execute(app, "publish", rootDir.toString());

        // Then
        verify(structurizrAdapter, never()).publish(any());
        collector.checkThat(statusCode, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("Invalid product-architecture.yml has 2 errors:\n$.description: is missing but it is required\n$.name: null found, string expected\n"));
    }

    @Test
    public void shouldDisplayStructurizrPublishError() {
        // Given
        StructurizrAdapter structurizrAdapter = mock(StructurizrAdapter.class);
        doThrow(new RuntimeException("Boom!")).when(structurizrAdapter).publish(any());

        Application app = Application.builder()
                .structurizrAdapter(structurizrAdapter)
                .build();
        Path rootDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath()).toPath();

        // When
        Integer statusCode = execute(app, "publish", rootDir.toString());

        // Then
        collector.checkThat(statusCode, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("Unable to publish to Structurizer\nError thrown: java.lang.RuntimeException: Boom!"));
    }
}
