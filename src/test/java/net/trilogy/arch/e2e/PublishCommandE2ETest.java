package net.trilogy.arch.e2e;

import com.structurizr.Workspace;
import net.trilogy.arch.Application;
import net.trilogy.arch.CommandTestBase;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION;
import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PublishCommandE2ETest extends CommandTestBase {
    @Test
    public void shouldSuccessfullyPublish() {
        // Given
        StructurizrAdapter structurizrAdapter = mock(StructurizrAdapter.class);
        when(structurizrAdapter.publish(any(Workspace.class))).thenReturn(true);

        Application app = Application.builder()
                .structurizrAdapter(structurizrAdapter)
                .build();
        Path rootDir = new File(getClass().getResource(ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath()).toPath();

        // When
        Integer statusCode = execute(app, "publish", rootDir.toString());

        // Then
        verify(structurizrAdapter, times(1)).publish(any());
        collector.checkThat(statusCode, equalTo(0));
        collector.checkThat(dummyOut.getLog(), equalTo("Successfully published to Structurizr!\n"));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldListValidationsErrorsWhenProductArchitectureInvalid() throws Exception {
        // Given
        // No need to mock publish() because it's never called due to the precondition check
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
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString("Invalid product-architecture.yml has 2 errors:\n$.description: is missing but it is required\n$.name: null found, string expected\n"));
    }

    @Test
    public void shouldDisplayStructurizrPublishError() {
        // Given
        StructurizrAdapter structurizrAdapter = mock(StructurizrAdapter.class);
        doThrow(new RuntimeException("Boom!")).when(structurizrAdapter).publish(any());

        Application app = Application.builder()
                .structurizrAdapter(structurizrAdapter)
                .build();
        Path rootDir = new File(getClass().getResource(ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath()).toPath();

        // When
        Integer statusCode = execute(app, "publish", rootDir.toString());

        // Then
        collector.checkThat(statusCode, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString("Unable to publish to Structurizer\nError: java.lang.RuntimeException: Boom!"));
    }
}
