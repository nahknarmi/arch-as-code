package net.trilogy.arch.adapter.architectureUpdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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

/** @todo Does Jackson provide a builder for this? */
public class ArchitectureUpdateObjectMapper {
    public static final ObjectMapper AU_OBJECT_MAPPER;

    static {
        final var mapper = new ObjectMapper(
                new YAMLFactory()
                        .configure(SPLIT_LINES, false)
                        .enable(MINIMIZE_QUOTES)
                        .enable(ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
                        .disable(WRITE_DOC_START_MARKER)
                        .configure(STRICT_DUPLICATE_DETECTION, true));
        mapper.setVisibility(FIELD, ANY);
        mapper.setVisibility(GETTER, NONE);
        mapper.setVisibility(IS_GETTER, NONE);
        mapper.setSerializationInclusion(NON_NULL);

        AU_OBJECT_MAPPER = mapper;
    }
}
