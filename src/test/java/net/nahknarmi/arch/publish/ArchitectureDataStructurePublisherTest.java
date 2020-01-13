package net.nahknarmi.arch.publish;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClientException;
import net.nahknarmi.arch.adapter.StructurizrAdapter;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.ArchitectureDataStructureReader;
import net.nahknarmi.arch.transformation.ArchitectureDataStructureTransformer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ArchitectureDataStructurePublisherTest {

    @Test
    public void given_file_that_exists_should_publish_workspace() throws IOException, StructurizrClientException {
        File productDocumentationRoot = mock(File.class);
        ArchitectureDataStructureReader importer = mock(ArchitectureDataStructureReader.class);
        ArchitectureDataStructureTransformer transformer = mock(ArchitectureDataStructureTransformer.class);
        StructurizrAdapter adapter = mock(StructurizrAdapter.class);

        //when
        when(productDocumentationRoot.exists()).thenReturn(true);
        new ArchitectureDataStructurePublisher(productDocumentationRoot, importer, transformer, adapter).publish();

        //then
        verify(importer, times(1)).load(any(File.class));
        verify(transformer, times(1)).toWorkSpace(any(ArchitectureDataStructure.class));
        verify(adapter, times(1)).publish(any(Workspace.class));
    }
}
