package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.publish.ArchitectureDataStructurePublisher;
import net.trilogy.arch.validation.ArchitectureDataStructureValidatorFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "publish", mixinStandardHelpOptions = true, description = "Publish architecture to structurizr.")
public class PublishCommand implements Callable<Integer>, DisplaysOutputMixin, DisplaysErrorMixin {
    private final StructurizrAdapter structurizrAdapter;

    @Parameters(index = "0", paramLabel = "PRODUCT_ARCHITECTURE_DIRECTORY", description = "Product architecture root where product-architecture.yml is located.")
    private File productArchitectureDirectory;

    @Getter
    @Spec
    private CommandLine.Model.CommandSpec spec;

    public PublishCommand() {
        structurizrAdapter = new StructurizrAdapter();
    }

    public PublishCommand(StructurizrAdapter structurizrAdapter) {
        this.structurizrAdapter = structurizrAdapter;
    }

    @Override
    public Integer call() {
        logArgs();
        List<String> messageSet;
        try {
            messageSet = ArchitectureDataStructureValidatorFactory.create().validate(productArchitectureDirectory, ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME);

            if (messageSet.isEmpty()) {
                new ArchitectureDataStructurePublisher(structurizrAdapter, new FilesFacade(), productArchitectureDirectory, ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME).publish();
                print("Successfully published to Structurizr!");
                return 0;
            }
        } catch (Exception e) {
            printError("Unable to publish to Structurizer", e);
            return 1;
        }

        printError(String.format("Invalid product-architecture.yml has %d errors:", messageSet.size()));
        messageSet.forEach(this::printError);
        return messageSet.size();
    }
}

