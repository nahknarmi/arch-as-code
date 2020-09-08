package net.trilogy.arch.adapter.architectureDataStructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;

import java.io.IOException;
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
    public static final ObjectMapper YAML_OBJECT_MAPPER = YAMLMapper.builder()
            .enable(MINIMIZE_QUOTES)
            .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            .configure(SPLIT_LINES, false)
            .disable(WRITE_DOC_START_MARKER)
            .configure(STRICT_DUPLICATE_DETECTION, true)
            .build()
            .setVisibility(FIELD, ANY)
            .setVisibility(GETTER, NONE)
            .setVisibility(IS_GETTER, NONE)
            .registerModule(dateSerializer())
            .registerModule(setSerializer())
            .setSerializationInclusion(NON_NULL);

    public ArchitectureDataStructure readValue(String architectureAsString) throws IOException {
        // TODO [ENHANCEMENT] [OPTIONAL]: Generate paths if they don't exist
        return YAML_OBJECT_MAPPER.readValue(architectureAsString, ArchitectureDataStructure.class);
    }

    /** TODO: Modern Jackson should provide UTC serialization out of the box */
    private static SimpleModule dateSerializer() {
        return new SimpleModule().addSerializer(new DateSerializer(Date.class));
    }

    /** TODO: Modern Jackson should provide Set serialization out of the box */
    private static SimpleModule setSerializer() {
        return new SimpleModule().addSerializer(new SetSerializer(Set.class));
    }
}
