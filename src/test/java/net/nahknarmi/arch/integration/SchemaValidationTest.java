package net.nahknarmi.arch.integration;

import com.networknt.schema.ValidationMessage;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static net.nahknarmi.arch.TestHelper.TEST_VALIDATION_ROOT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;


public class SchemaValidationTest {

    @Test
    public void validate_missing_system() throws IOException {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("missingMetadata.yml");

        assertThat(validationMessageSet.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                containsInAnyOrder(
                        "$.name: null found, string expected",
                        "$.description: is missing but it is required",
                        "$.businessUnit: is missing but it is required"
                ));
    }

    private Set<ValidationMessage> getSchemaValidationMessages(String yamlFileName) throws FileNotFoundException {
        File validationRoot = new File(getClass().getResource(TEST_VALIDATION_ROOT_PATH).getPath());
        File yamlFile = new File(validationRoot + File.separator + yamlFileName);
        return new ArchitectureDataStructureSchemaValidator()
                .validate(new FileInputStream(yamlFile));
    }

}
