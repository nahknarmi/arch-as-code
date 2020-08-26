package net.trilogy.arch.adapter.architectureDataStructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
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

public class ArchitectureDataStructureObjectMapper {
    private final ObjectMapper mapper;

    public ArchitectureDataStructureObjectMapper() {
        this.mapper = new ObjectMapper(
                new YAMLFactory()
                        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                        .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
                        .configure(YAMLGenerator.Feature.SPLIT_LINES, false)
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        );
        this.mapper.setVisibility(FIELD, ANY);
        this.mapper.setVisibility(GETTER, NONE);
        this.mapper.setVisibility(IS_GETTER, NONE);
        this.mapper.registerModule(dateSerializer());
        this.mapper.registerModule(setSerializer());
        this.mapper.setSerializationInclusion(NON_NULL);
    }

    public String writeValueAsString(ArchitectureDataStructure value) throws IOException {
        return this.mapper.writeValueAsString(value);
    }

    public JsonNode readTree(@NotNull InputStream in) throws IOException {
        return this.mapper.readTree(in);
    }

    public ArchitectureDataStructure readValue(String architectureAsString) throws IOException {
        // TODO [ENHANCEMENT] [OPTIONAL]: Generate paths if they don't exist
        return this.mapper.readValue(architectureAsString, ArchitectureDataStructure.class);
    }

    private SimpleModule dateSerializer() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new DateSerializer(Date.class));
        return module;
    }

    private SimpleModule setSerializer() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new SetSerializer(Set.class));
        return module;
    }
}
