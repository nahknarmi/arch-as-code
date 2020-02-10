package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Location.UNSPECIFIED;

public class C4EntitySerializer<T extends BaseEntity> extends StdSerializer<T> {
    private static final Log log = LogFactory.getLog(C4EntitySerializer.class);

    public C4EntitySerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        baseEntityWrite(value, gen);

        gen.writeEndObject();
    }

    protected void baseEntityWrite(T value, JsonGenerator gen) throws IOException {
        gen.writeStringField("name", value.name());
        gen.writeStringField("path", value.getPath().getPath());
        gen.writeStringField("description", value.getDescription());

        writeTags(value, gen);
        writeRelationships(value, gen);

        writeLocation(value, gen);
        writeUrl(value, gen);
        writeTechnology(value, gen);
    }

    private void writeTags(T value, JsonGenerator gen) throws IOException {
        gen.writeFieldName("tags");
        gen.writeStartArray();
        value.getTags().forEach(t -> {
            try {
                gen.writeString(t.getTag());
            } catch (IOException e) {
                log.error("Failed to write tags.", e);
                throw new IllegalStateException("Failed to write tags.", e);
            }
        });
        gen.writeEndArray();
    }

    private void writeRelationships(T value, JsonGenerator gen) throws IOException {
        gen.writeFieldName("relationships");
        gen.writeStartArray();
        value.getRelationships().forEach(x -> {
            try {
                @NonNull C4Path with = x.getWith();
                @NonNull C4Action action = x.getAction();

                gen.writeStartObject();
                gen.writeStringField("with", with.getPath());
                gen.writeStringField("action", action.name());
                gen.writeStringField("description", x.getDescription());
                ofNullable(x.getTechnology()).ifPresent((t) -> wrappedWriteStringField(gen, "technology", t));
                gen.writeEndObject();

            } catch (IOException e) {
                log.error("Failed to write relationships.", e);
                throw new IllegalStateException("Failed to write relationships.", e);
            }
        });
        gen.writeEndArray();
    }

    public static void wrappedWriteStringField(JsonGenerator gen, String fieldName, String fieldValue) {
        try {
            if (fieldValue != null) {
                gen.writeStringField(fieldName, fieldValue);
            }
        } catch (IOException e) {
            log.error("Failed to write field - " + fieldName, e);
            throw new IllegalStateException("Failed to write field - " + fieldName);
        }
    }

    private void writeTechnology(T value, JsonGenerator gen) {
        if (value instanceof HasTechnology) {
            ofNullable(((HasTechnology) value).getTechnology()).ifPresent((x) -> wrappedWriteStringField(gen, "technology", x));
        }
    }

    private void writeUrl(T value, JsonGenerator gen) {
        if (value instanceof HasUrl) {
            ofNullable(((HasUrl) value).getUrl()).ifPresent((x) -> wrappedWriteStringField(gen, "url", x));
        }
    }

    private void writeLocation(T value, JsonGenerator gen) throws IOException {
        if (value instanceof HasLocation) {
            gen.writeStringField("location", ofNullable(((HasLocation) value).getLocation()).orElse(UNSPECIFIED).name());
        }
    }
}
