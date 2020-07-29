package net.trilogy.arch.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.view.ViewSet;
import net.trilogy.arch.adapter.architectureYaml.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.transformation.ArchitectureDataStructureTransformer;
import net.trilogy.arch.transformation.TransformerFactory;

import java.io.File;
import java.io.IOException;

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
        this.dataStructureTransformer = TransformerFactory.create(productArchitectureDirectory);
        this.structurizrAdapter = new StructurizrAdapter();
    }

    public ArchitectureDataStructurePublisher(StructurizrAdapter structurizrAdapter,
                                              FilesFacade filesFacade,
                                              File productArchitectureDirectory,
                                              String manifestFileName) {
        this.filesFacade = filesFacade;
        this.productArchitectureDirectory = productArchitectureDirectory;
        this.manifestFileName = manifestFileName;
        this.dataStructureTransformer = TransformerFactory.create(productArchitectureDirectory);
        this.structurizrAdapter = structurizrAdapter;
    }

    public void publish() throws IOException {
        Workspace workspace = getWorkspace(productArchitectureDirectory, manifestFileName);

        loadAndSetViews(productArchitectureDirectory, workspace);

        if (!structurizrAdapter.publish(workspace)) {
            throw new RuntimeException("Failed to publish to Structurizr");
        }
    }

    private void savePublishedWorkspace(Workspace workspace) throws IOException {
        File published = new File(productArchitectureDirectory + File.separator + "published.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(published, workspace);
    }

    public Workspace getWorkspace(File productArchitectureDirectory, String manifestFileName) throws IOException {
        ArchitectureDataStructure dataStructure = loadProductArchitecture(productArchitectureDirectory, manifestFileName);

        return dataStructureTransformer.toWorkSpace(dataStructure);
    }

    public ArchitectureDataStructure loadProductArchitecture(File productArchitectureDirectory, String manifestFileName) throws IOException {
        File manifestFile = new File(productArchitectureDirectory + File.separator + manifestFileName);
        String archAsString = this.filesFacade.readString(manifestFile.toPath());

        return new ArchitectureDataStructureObjectMapper().readValue(archAsString);
    }

    private void loadAndSetViews(File productArchitectureDirectory, Workspace workspace) throws IOException {
        ViewSet viewSet = loadStructurizrViews(productArchitectureDirectory);

        new ViewReferenceMapper().addViewsWithReferencedObjects(workspace, viewSet);
    }

    private ViewSet loadStructurizrViews(File productArchitectureDirectory) throws IOException {
        File manifestFile = new File(productArchitectureDirectory + File.separator + "structurizrViews.json");
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(manifestFile, ViewSet.class);
    }
}
