package net.trilogy.arch.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;

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
        final var schema = getJsonSchemaFromClasspath(schemaResource);

        try {
            final var node = getYamlFromFile(manifestInputStream);
            return schema.validate(node);
        } catch (IOException e) {
            throw new IllegalStateException(errorMessage, e);
        }
    }

    @SuppressWarnings("deprecation")
    private JsonSchema getJsonSchemaFromClasspath(String schemaResource) {
        // TODO: See if Jackson has a non-deprecated means of doing this
        final var factory = JsonSchemaFactory.getInstance();
        final var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaResource);
        return factory.getSchema(is);
    }

    private JsonNode getYamlFromFile(InputStream manifestInputStream) throws IOException {
        return YAML_OBJECT_MAPPER.readTree(manifestInputStream);
    }
}
