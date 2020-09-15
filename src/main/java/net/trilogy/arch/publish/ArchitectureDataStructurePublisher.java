package net.trilogy.arch.publish;

import com.structurizr.Workspace;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.transformation.ArchitectureDataStructureTransformer;
import net.trilogy.arch.transformation.TransformerFactory;

import java.io.File;
import java.io.IOException;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;

public class ArchitectureDataStructurePublisher {
    private final File productArchitectureDirectory;
    private final FilesFacade filesFacade;
    private final ArchitectureDataStructureTransformer dataStructureTransformer;
    private final StructurizrAdapter structurizrAdapter;
    private final String manifestFileName;

    public ArchitectureDataStructurePublisher(FilesFacade filesFacade,
                                              File productArchitectureDirectory,
                                              String manifestFileName) {
        this.filesFacade = filesFacade;
        this.productArchitectureDirectory = productArchitectureDirectory;
        this.manifestFileName = manifestFileName;
        dataStructureTransformer = TransformerFactory.create(productArchitectureDirectory);
        structurizrAdapter = new StructurizrAdapter();
    }

    public ArchitectureDataStructurePublisher(StructurizrAdapter structurizrAdapter,
                                              FilesFacade filesFacade,
                                              File productArchitectureDirectory,
                                              String manifestFileName) {
        this.filesFacade = filesFacade;
        this.productArchitectureDirectory = productArchitectureDirectory;
        this.manifestFileName = manifestFileName;
        dataStructureTransformer = TransformerFactory.create(productArchitectureDirectory);
        this.structurizrAdapter = structurizrAdapter;
    }

    public void publish() throws IOException {
        final var workspace = getWorkspace(productArchitectureDirectory, manifestFileName);

        if (!structurizrAdapter.publish(workspace)) {
            throw new RuntimeException("Failed to publish to Structurizr");
        }
    }

    public Workspace getWorkspace(File productArchitectureDirectory, String manifestFileName) throws IOException {
        final var dataStructure = loadProductArchitecture(productArchitectureDirectory, manifestFileName);

        return dataStructureTransformer.toWorkSpace(dataStructure);
    }

    public ArchitectureDataStructure loadProductArchitecture(File productArchitectureDirectory, String manifestFileName) throws IOException {
        final var manifestFile = new File(productArchitectureDirectory + File.separator + manifestFileName);
        final var architectureAsString = filesFacade.readString(manifestFile.toPath());

        return YAML_OBJECT_MAPPER.readValue(architectureAsString, ArchitectureDataStructure.class);
    }
}
