package net.trilogy.arch.publish;

import com.structurizr.Workspace;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import net.trilogy.arch.domain.ArchitectureDataStructure;
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
        File productArchitectureDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_GENERALLY).getPath());
        ArchitectureDataStructurePublisher publisher = new ArchitectureDataStructurePublisher(
                new StructurizrAdapter(),
                new FilesFacade(),
                productArchitectureDir,
                "product-architecture.yml"
        );

        // When
        ArchitectureDataStructure dataStructure = publisher.loadProductArchitecture(productArchitectureDir, "product-architecture.yml");

        // Then
        assertThat(dataStructure.getName(), equalTo("TestSpaces"));
    }

    @Test
    public void shouldPublishWorkspace() throws Exception {
        // Given
        StructurizrAdapter mockedStructurizrAdapter = mock(StructurizrAdapter.class);
        ArgumentCaptor<Workspace> workspaceArgumentCaptor = ArgumentCaptor.forClass(Workspace.class);

        File productArchitectureDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_GENERALLY).getPath());
        String manifestFileName = "product-architecture.yml";
        ArchitectureDataStructurePublisher publisher = new ArchitectureDataStructurePublisher(
                mockedStructurizrAdapter,
                new FilesFacade(),
                productArchitectureDir,
                manifestFileName);

        Workspace expectedWorkspace = publisher.getWorkspace(productArchitectureDir, manifestFileName);

        when(mockedStructurizrAdapter.publish(any(Workspace.class))).thenReturn(true);

        // When
        publisher.publish();

        // Then
        verify(mockedStructurizrAdapter).publish(workspaceArgumentCaptor.capture());
        Workspace actualWorkspace = workspaceArgumentCaptor.getValue();

        assertThat(actualWorkspace.getName(), equalTo(expectedWorkspace.getName()));
        assertThat(actualWorkspace.getId(), equalTo(expectedWorkspace.getId()));
    }
}
