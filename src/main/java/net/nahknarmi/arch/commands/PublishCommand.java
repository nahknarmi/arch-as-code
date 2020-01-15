package net.nahknarmi.arch.commands;

import com.structurizr.api.StructurizrClientException;
import net.nahknarmi.arch.publish.ArchitectureDataStructurePublisher;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "publish", description = "Publishes to structurizr")
public class PublishCommand implements Callable<Integer> {
    private final String manifestFileName;

    @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_PATH", description = "Product documentation root where data-structure.yml is located.", defaultValue = "./")
    private File productDocumentationRoot;

    // Only for testing purposes
    public PublishCommand(File productDocumentationRoot, String manifestFileName) {
        this.productDocumentationRoot = productDocumentationRoot;
        this.manifestFileName = manifestFileName;
    }

    public PublishCommand() {
        this.manifestFileName = "data-structure.yml";
    }

    @Override
    public Integer call() throws IOException, StructurizrClientException {
        if (new ValidateCommand(productDocumentationRoot, manifestFileName).call() == 0) {
            ArchitectureDataStructurePublisher.create(productDocumentationRoot).publish();
            return 0;
        }
        return 1;
    }
}
