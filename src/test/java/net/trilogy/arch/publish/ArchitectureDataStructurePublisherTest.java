package net.trilogy.arch.publish;

import com.structurizr.Workspace;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ArchitectureDataStructurePublisherTest {
    @Test
    public void shouldLoadProductArchitecture() throws Exception {
        // Given
        final var productArchitectureDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_GENERALLY).getPath());
        final var publisher = new ArchitectureDataStructurePublisher(
                new StructurizrAdapter(),
                new FilesFacade(),
                productArchitectureDir,
                "product-architecture.yml");

        // When
        final var dataStructure = publisher.loadProductArchitecture(productArchitectureDir, "product-architecture.yml");

        // Then
        assertThat(dataStructure.getName(), equalTo("TestSpaces"));
    }

    @Test
    public void shouldPublishWorkspace() throws Exception {
        // Given
        final var mockedStructurizrAdapter = mock(StructurizrAdapter.class);
        final var workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);

        final var productArchitectureDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_GENERALLY).getPath());
        final var manifestFileName = "product-architecture.yml";
        final var publisher = new ArchitectureDataStructurePublisher(
                mockedStructurizrAdapter,
                new FilesFacade(),
                productArchitectureDir,
                manifestFileName);

        final var expectedWorkspace = publisher.getWorkspace(productArchitectureDir, manifestFileName);

        when(mockedStructurizrAdapter.publish(any(Workspace.class))).thenReturn(true);

        // When
        publisher.publish();

        // Then
        verify(mockedStructurizrAdapter).publish(workspaceArgumentCaptor.capture());
        final var actualWorkspace = workspaceArgumentCaptor.getValue();

        assertThat(actualWorkspace.getName(), equalTo(expectedWorkspace.getName()));
        assertThat(actualWorkspace.getId(), equalTo(expectedWorkspace.getId()));
    }
}
