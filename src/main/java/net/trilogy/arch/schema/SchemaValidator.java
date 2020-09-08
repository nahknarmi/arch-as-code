package net.trilogy.arch.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class SchemaValidator {
    public static final String ARCH_DOC_SCHEMA = "schema/dataStructureSchema.json";
    public static final String ARCH_UPDATE_DOC_SCHEMA = "schema/architectureUpdateSchema.json";

    public Set<ValidationMessage> validateArchitectureDocument(InputStream manifestInputStream) {
        return validate(manifestInputStream, ARCH_DOC_SCHEMA, "Product architecture data structure yaml file could not be found.");
    }

    public Set<ValidationMessage> validateArchitectureUpdateDocument(InputStream manifestInputStream) {
        return validate(manifestInputStream, ARCH_UPDATE_DOC_SCHEMA, "Architecture update yaml file could not be found.");
    }

    private Set<ValidationMessage> validate(InputStream manifestInputStream, String schemaResource, String errorMessage) {
        JsonSchema schema = getJsonSchemaFromClasspath(schemaResource);
        Set<ValidationMessage> errors;

        try {
            JsonNode node = getYamlFromFile(manifestInputStream);
            errors = schema.validate(node);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
        return errors;
    }

    @SuppressWarnings("deprecation")
    private JsonSchema getJsonSchemaFromClasspath(String schemaResource) {
        // TODO: See if Jackson has a non-deprecated means of doing this
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaResource);
        return factory.getSchema(is);
    }

    private JsonNode getYamlFromFile(InputStream manifestInputStream) throws IOException {
        return new ArchitectureDataStructureObjectMapper().readTree(manifestInputStream);
    }
}
