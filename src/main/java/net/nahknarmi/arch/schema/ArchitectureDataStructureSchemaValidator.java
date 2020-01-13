package net.nahknarmi.arch.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ArchitectureDataStructureSchemaValidator {

    public Set<ValidationMessage> validate(InputStream manifestInputStream) {
        JsonSchema schema = getJsonSchemaFromClasspath();
        Set<ValidationMessage> errors;

        try {
            JsonNode node = getYamlFromFile(manifestInputStream);
            errors = schema.validate(node);
        } catch (IOException e) {
            throw new IllegalStateException("Product documentation data structure yaml file could not be found.", e);
        }

        return errors;
    }

    private JsonSchema getJsonSchemaFromClasspath() {
        String schemaResource = "schema/dataStructureSchema.json";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaResource);
        return factory.getSchema(is);
    }

    private JsonNode getYamlFromFile(InputStream manifestInputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readTree(manifestInputStream);
    }
}
