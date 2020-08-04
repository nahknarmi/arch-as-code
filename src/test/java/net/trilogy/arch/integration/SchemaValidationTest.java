package net.trilogy.arch.integration;

import com.networknt.schema.ValidationMessage;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.schema.SchemaValidator;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;


public class SchemaValidationTest {

    @Test
    public void validate_all_valid_schema() throws Exception {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("allValidSchema.yml");

        assertThat(validationMessageSet, Matchers.emptyIterable());
    }

    @Test
    public void validate_missing_name_and_description() throws Exception {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("missingMetadata.yml");

        assertThat(validationMessageSet.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                containsInAnyOrder(
                        "$.name: null found, string expected",
                        "$.description: is missing but it is required"
                ));
    }

    @Test
    public void validate_missing_decision_log() throws Exception {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("missingDecisionLog.yml");

        assertThat(validationMessageSet.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                containsInAnyOrder(
                        "$.decisions[0].id: is missing but it is required",
                        "$.decisions[0].date: is missing but it is required",
                        "$.decisions[0].title: is missing but it is required",
                        "$.decisions[0].status: is missing but it is required",
                        "$.decisions[0].content: is missing but it is required"
                ));
    }

    @Test
    public void validate_missing_model_properties() throws Exception {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("missingModelProperties.yml");

        assertThat(validationMessageSet.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                containsInAnyOrder("$.model.people: is missing but it is required",
                        "$.model.systems: is missing but it is required"
                ));
    }


    private Set<ValidationMessage> getSchemaValidationMessages(String yamlFileName) throws FileNotFoundException {
        File validationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_VALIDATION).getPath());
        File yamlFile = new File(validationRoot + File.separator + yamlFileName);
        return new SchemaValidator()
                .validateArchitectureDocument(new FileInputStream(yamlFile));
    }

}
