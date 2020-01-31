package net.nahknarmi.arch.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import org.junit.Test;

import java.io.IOException;

public class SchemaGenerationTest {

    @Test
    public void generate_schema() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        JsonSchema jsonSchema = schemaGen.generateSchema(ArchitectureDataStructure.class);

        String valueAsString =
                new ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(jsonSchema);

        System.err.println(valueAsString);
    }
}
