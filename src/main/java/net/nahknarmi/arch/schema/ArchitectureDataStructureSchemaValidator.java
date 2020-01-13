package net.nahknarmi.arch.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class ArchitectureDataStructureSchemaValidator {

    public boolean validate(File productDocumentationRoot) {
        JsonSchema schema = getJsonSchemaFromClasspath();

        try {
            JsonNode node = getYamlFromFile(productDocumentationRoot);
            Set<ValidationMessage> errors = schema.validate(node);

            errors.forEach(e -> System.err.println(e.getMessage()));

            if (!errors.isEmpty()) {
                return false;
            }

        } catch (IOException e) {
            throw new IllegalStateException("Product documentation data structure yaml file could not be found.", e);
        }

        return true;
    }

    private JsonSchema getJsonSchemaFromClasspath() {
        String schemaResource = "schema/dataStructureSchema.json";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaResource);
        return factory.getSchema(is);
    }

    private JsonNode getYamlFromFile(File productArchitecturePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File dataStructureFile = new File(productArchitecturePath.getAbsolutePath() + File.separator + "data-structure.yml");
        return mapper.readTree(dataStructureFile);
    }
}
