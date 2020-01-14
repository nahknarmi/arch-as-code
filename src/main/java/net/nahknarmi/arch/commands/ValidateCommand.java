package net.nahknarmi.arch.commands;

import com.networknt.schema.ValidationMessage;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;

@CommandLine.Command(name = "validate", description = "Validate product architecture yaml")
public class ValidateCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_PATH", description = "Product documentation root where data-structure.yml is located.")
    private File productDocumentationRoot;

    @Override
    public Integer call() throws FileNotFoundException {
        checkArgument(productDocumentationRoot.exists(), "Product documentation root does not exist.");
        checkArgument(productDocumentationRoot.isDirectory(), "Product documentation root does not exist.");

        Set<ValidationMessage> messageSet = new ArchitectureDataStructureSchemaValidator().validate(new FileInputStream(productDocumentationRoot));

        if (messageSet.isEmpty()) {
            System.out.println("Valid yaml");
        } else {
            messageSet.forEach(x -> System.err.println(x.getMessage()));
            return 1;
        }

        return 0;
    }
}
