package net.nahknarmi.arch.commands;

import com.google.common.base.Preconditions;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;

@CommandLine.Command(name = "validate", description = "Validate product architecture yaml")
public class ValidateCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_PATH", description = "Product documentation root where data-structure.yml is located.")
    private File productDocumentationRoot;

    @Override
    public Integer call() {
        checkArgument(productDocumentationRoot.exists(), "Product documentation root does not exist.");
        checkArgument(productDocumentationRoot.isDirectory(), "Product documentation root does not exist.");

        boolean validate = new ArchitectureDataStructureSchemaValidator().validate(productDocumentationRoot);

        if (validate) {
            System.out.println("Valid yaml");
        } else {
            return 1;
        }

        return 0;
    }
}
