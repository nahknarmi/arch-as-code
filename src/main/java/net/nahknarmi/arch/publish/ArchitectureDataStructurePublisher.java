package net.nahknarmi.arch.publish;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClientException;
import net.nahknarmi.arch.adapter.StructurizrAdapter;
import net.nahknarmi.arch.adapter.WorkspaceIdFinder;
import net.nahknarmi.arch.adapter.in.ArchitectureDataStructureReader;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.transformation.ArchitectureDataStructureTransformer;
import net.nahknarmi.arch.transformation.TransformerFactory;

import java.io.File;
import java.io.IOException;

public class ArchitectureDataStructurePublisher {
    private final File productDocumentationRoot;
    private final ArchitectureDataStructureReader dataStructureReader;
    private final ArchitectureDataStructureTransformer dataStructureTransformer;
    private final StructurizrAdapter structurizrAdapter;
    private final String manifestFileName;

    ArchitectureDataStructurePublisher(File productDocumentationRoot,
                                              ArchitectureDataStructureReader importer,
                                              ArchitectureDataStructureTransformer transformer,
                                              StructurizrAdapter structurizrAdapter) {
        this.productDocumentationRoot = productDocumentationRoot;
        this.dataStructureTransformer = transformer;
        this.dataStructureReader = importer;
        this.structurizrAdapter = structurizrAdapter;
        this.manifestFileName = "data-structure.yml";

    }

    public ArchitectureDataStructurePublisher(File productDocumentationRoot, ArchitectureDataStructureReader importer, ArchitectureDataStructureTransformer transformer, StructurizrAdapter adapter, String manifestFileName) {
        this.productDocumentationRoot = productDocumentationRoot;
        this.dataStructureTransformer = transformer;
        this.dataStructureReader = importer;
        this.structurizrAdapter = adapter;
        this.manifestFileName = manifestFileName;
    }

    public void publish() throws StructurizrClientException, IOException {
        Workspace workspace = getWorkspace(productDocumentationRoot, manifestFileName);
        structurizrAdapter.publish(workspace);
    }

    public Workspace getWorkspace(File productDocumentationRoot, String manifestFileName) throws IOException {
        File manifestFile = new File(productDocumentationRoot + File.separator + manifestFileName);
        ArchitectureDataStructure dataStructure = dataStructureReader.load(manifestFile);

        return dataStructureTransformer.toWorkSpace(dataStructure);
    }

    public static ArchitectureDataStructurePublisher create(File productDocumentationRoot, String manifestFileName) {
        ArchitectureDataStructureReader importer = new ArchitectureDataStructureReader();
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(productDocumentationRoot);
        WorkspaceIdFinder workspaceIdFinder = new WorkspaceIdFinder();
        StructurizrAdapter adapter = new StructurizrAdapter(workspaceIdFinder);

        return new ArchitectureDataStructurePublisher(productDocumentationRoot, importer, transformer, adapter, manifestFileName);
    }
}
