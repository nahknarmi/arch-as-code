package net.trilogy.arch.validation;

import com.networknt.schema.ValidationMessage;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureReader;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.schema.SchemaValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("deprecation") // ArchitectureDataStructureReader
public class ArchitectureDataStructureValidator {
    private final List<DataStructureValidator> dataStructureDataStructureValidators;
    private final SchemaValidator schemaValidator;
    private final ArchitectureDataStructureReader dataStructureReader;

    public ArchitectureDataStructureValidator(List<DataStructureValidator> dataStructureDataStructureValidators, SchemaValidator schemaValidator, ArchitectureDataStructureReader dataStructureReader) {
        this.dataStructureDataStructureValidators = dataStructureDataStructureValidators;
        this.schemaValidator = schemaValidator;
        this.dataStructureReader = dataStructureReader;
    }

    // TODO [TESTING]: Add tests
    // Test 1: run this method, make sure some errors are caught (we're just testing that it's catching _something_, proving that it's using the schema validator, which is already tested)
    // Test 2: run this method, make sure schema validator is called (but don't have strict assertions-- any call is fine)
    public List<String> validate(File productArchitectureDirectory, String manifestFileName) throws IOException {
        File manifestFile = new File(productArchitectureDirectory.getAbsolutePath() + File.separator + manifestFileName);
        checkArgument(manifestFile.exists(), String.format("Product Architecture manifest file %s does not exist.", manifestFile.getAbsolutePath()));

        List<String> schemaValidationMessages = this.schemaValidator.validateArchitectureDocument(new FileInputStream(manifestFile)).stream().map(ValidationMessage::getMessage).collect(toList());

        if (schemaValidationMessages.isEmpty()) {
            ArchitectureDataStructure dataStructure = dataStructureReader.load(manifestFile);
            return this.dataStructureDataStructureValidators.stream().flatMap(v -> v.validate(dataStructure).stream()).collect(toList());
        } else {
            return schemaValidationMessages;
        }
    }
}
