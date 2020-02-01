package net.nahknarmi.arch.commands;

import net.nahknarmi.arch.validation.ArchitectureDataStructureValidatorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "validate", description = "Validate product architecture yaml")
public class ValidateCommand implements Callable<Integer> {
    private static final Log logger = LogFactory.getLog(ValidateCommand.class);

    @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_PATH", description = "Product documentation root where data-structure.yml is located.")
    File productDocumentationRoot;
    private final String manifestFileName;

    // Only for testing purposes
    public ValidateCommand(File productDocumentationRoot, String manifestFileName) {
        this.productDocumentationRoot = productDocumentationRoot;
        this.manifestFileName = manifestFileName;
    }

    // For production
    public ValidateCommand() {
        this.manifestFileName = "data-structure.yml";
    }

    @Override
    public Integer call() throws IOException {
        List<String> messageSet = ArchitectureDataStructureValidatorFactory.create().validate(productDocumentationRoot, this.manifestFileName);

        if (messageSet.isEmpty()) {
            logger.info(manifestFileName + " is valid.");
        } else {
            logger.error(manifestFileName + " is invalid.");
            messageSet.forEach(logger::error);
        }

        return messageSet.size();
    }
}
