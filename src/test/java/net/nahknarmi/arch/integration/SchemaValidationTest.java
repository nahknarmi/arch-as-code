package net.nahknarmi.arch.integration;

import com.networknt.schema.ValidationMessage;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;
import org.hamcrest.Matchers;
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
    public void validate_all_valid_schema() throws IOException {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("allValidSchema.yml");

        assertThat(validationMessageSet, Matchers.emptyIterable());
    }

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

    @Test
    public void validate_missing_decision_log() throws IOException {
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
    public void validate_missing_model_properties() throws IOException {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("missingModelProperties.yml");

        assertThat(validationMessageSet.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                containsInAnyOrder("$.model.people: is missing but it is required",
                        "$.model.systems: is missing but it is required"
                ));
    }

    @Test
    public void validate_missing_view_contexts() throws IOException {
        Set<ValidationMessage> validationMessageSet = getSchemaValidationMessages("missingViewContexts.yml");

        assertThat(validationMessageSet.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                containsInAnyOrder(
                        "$.model.views.systemViews[0].name: is missing but it is required",
                        "$.model.views.systemViews[0].systemPath: is missing but it is required",
                        "$.model.views.systemViews[0].description: is missing but it is required",
                        "$.model.views.containerViews[0].name: is missing but it is required",
                        "$.model.views.containerViews[0].systemPath: is missing but it is required",
                        "$.model.views.containerViews[0].description: is missing but it is required",
                        "$.model.views.componentViews[0].name: is missing but it is required",
                        "$.model.views.componentViews[0].containerPath: is missing but it is required",
                        "$.model.views.componentViews[0].description: is missing but it is required"
                ));
    }

    private Set<ValidationMessage> getSchemaValidationMessages(String yamlFileName) throws FileNotFoundException {
        File validationRoot = new File(getClass().getResource(TEST_VALIDATION_ROOT_PATH).getPath());
        File yamlFile = new File(validationRoot + File.separator + yamlFileName);
        return new ArchitectureDataStructureSchemaValidator()
                .validate(new FileInputStream(yamlFile));
    }

}
