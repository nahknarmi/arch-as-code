package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.validation.ArchitectureDataStructureValidatorFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "validate", mixinStandardHelpOptions = true, description = "Validate a product's architecture")
public class ValidateCommand implements Callable<Integer>, DisplaysOutputMixin, DisplaysErrorMixin {
    @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_ARCHITECTURE_PATH", description = "Product architecture root where product-architecture.yml is located.")
    File productArchitectureDirectory;

    @Getter
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    // TODO [TESTING]: add sad path coverage e2e tests
    public Integer call() {
        logArgs();
        var fileName = ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME;
        List<String> messageSet;
        try {
            messageSet = ArchitectureDataStructureValidatorFactory.create().validate(productArchitectureDirectory, fileName);
        } catch (Exception e) {
            printError("", e);
            return 1;
        }

        if (messageSet.isEmpty()) {
            print(fileName + " is valid.");
            return 0;
        }

        printError(String.format("%s is invalid. (%d)", fileName, messageSet.size()));
        messageSet.forEach(this::printError);
        return messageSet.size();
    }
}
