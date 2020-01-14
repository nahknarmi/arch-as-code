package net.nahknarmi.arch.commands;

import com.structurizr.api.StructurizrClientException;
import net.nahknarmi.arch.publish.ArchitectureDataStructurePublisher;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "publish", description = "Publishes to structurizr")
public class PublishCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_PATH", description = "Product documentation root where data-structure.yml is located.", defaultValue = "./")
    File productDocumentationRoot;

    @Override
    public Integer call() throws IOException, StructurizrClientException {
        ArchitectureDataStructurePublisher.create(productDocumentationRoot).publish();
        return 0;
    }
}
