package net.nahknarmi.arch.validation;

import com.networknt.schema.ValidationMessage;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.ArchitectureDataStructureReader;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

public class ArchitectureDataStructureValidator {
    private final List<DataStructureValidator> dataStructureDataStructureValidators;
    private final ArchitectureDataStructureSchemaValidator architectureDataStructureSchemaValidator;
    private final ArchitectureDataStructureReader dataStructureReader;

    public ArchitectureDataStructureValidator(List<DataStructureValidator> dataStructureDataStructureValidators, ArchitectureDataStructureSchemaValidator architectureDataStructureSchemaValidator, ArchitectureDataStructureReader dataStructureReader) {
        this.dataStructureDataStructureValidators = dataStructureDataStructureValidators;
        this.architectureDataStructureSchemaValidator = architectureDataStructureSchemaValidator;
        this.dataStructureReader = dataStructureReader;
    }

    public List<String> validate(File productDocumentationRoot, String manifestFileName) throws IOException {
        File manifestFile = new File(productDocumentationRoot.getAbsolutePath() + File.separator + manifestFileName);
        checkArgument(manifestFile.exists(), String.format("Product Architecture manifest file %s does not exist.", manifestFile.getAbsolutePath()));

        List<String> schemaValidationMessages = this.architectureDataStructureSchemaValidator.validate(new FileInputStream(manifestFile)).stream().map(ValidationMessage::getMessage).collect(toList());

        if (schemaValidationMessages.isEmpty()) {
            ArchitectureDataStructure dataStructure = dataStructureReader.load(manifestFile);
            return this.dataStructureDataStructureValidators.stream().flatMap(v -> v.validate(dataStructure).stream()).collect(toList());
        } else {
            return schemaValidationMessages;
        }
    }

}
