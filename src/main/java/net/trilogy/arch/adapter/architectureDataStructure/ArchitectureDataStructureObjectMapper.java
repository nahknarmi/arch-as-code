package net.trilogy.arch.adapter.architectureDataStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.trilogy.arch.domain.ArchitectureDataStructure;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.annotation.PropertyAccessor.GETTER;
import static com.fasterxml.jackson.annotation.PropertyAccessor.IS_GETTER;
import static com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

public class ArchitectureDataStructureObjectMapper {
    public static final ObjectMapper YAML_OBJECT_MAPPER = createObjectMapper();

    /** @todo Does Jackson provide a builder for this? */
    private static ObjectMapper createObjectMapper() {
        final var mapper = new ObjectMapper(new YAMLFactory()
                .enable(MINIMIZE_QUOTES)
                .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
                .configure(SPLIT_LINES, false)
                .disable(WRITE_DOC_START_MARKER)
                .configure(STRICT_DUPLICATE_DETECTION, true));
        mapper.setVisibility(FIELD, ANY);
        mapper.setVisibility(GETTER, NONE);
        mapper.setVisibility(IS_GETTER, NONE);
        mapper.registerModule(dateSerializer());
        mapper.registerModule(setSerializer());
        mapper.setSerializationInclusion(NON_NULL);

        return mapper;
    }

    public String writeValueAsString(ArchitectureDataStructure value) throws IOException {
        return YAML_OBJECT_MAPPER.writeValueAsString(value);
    }

    public JsonNode readTree(@NotNull InputStream in) throws IOException {
        return YAML_OBJECT_MAPPER.readTree(in);
    }

    public ArchitectureDataStructure readValue(String architectureAsString) throws IOException {
        // TODO [ENHANCEMENT] [OPTIONAL]: Generate paths if they don't exist
        return YAML_OBJECT_MAPPER.readValue(architectureAsString, ArchitectureDataStructure.class);
    }

    private static SimpleModule dateSerializer() {
        final var module = new SimpleModule();
        module.addSerializer(new DateSerializer(Date.class));
        return module;
    }

    private static SimpleModule setSerializer() {
        final var module = new SimpleModule();
        module.addSerializer(new SetSerializer(Set.class));
        return module;
    }
}
