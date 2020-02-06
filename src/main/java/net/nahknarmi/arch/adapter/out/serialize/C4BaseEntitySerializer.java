package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.BaseEntity;
import net.nahknarmi.arch.domain.c4.C4Action;
import net.nahknarmi.arch.domain.c4.C4Path;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import static java.util.Optional.ofNullable;

abstract class C4BaseEntitySerializer<T extends BaseEntity> extends StdSerializer<T> {

    private final Log log = LogFactory.getLog(getClass());

    public C4BaseEntitySerializer(Class<T> t) {
        super(t);
    }

    protected void baseEntityWrite(T value, JsonGenerator gen) throws IOException {
        gen.writeStringField("path", value.getPath().getPath());
        gen.writeStringField("description", value.getDescription());

        writeTags(value, gen);
        writeRelationships(value, gen);
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
                ofNullable(x.getTechnology()).ifPresent((t) -> writeOptionalStringField(gen, "technology", t));
                gen.writeEndObject();

            } catch (IOException e) {
                log.error("Failed to write relationships.", e);
                throw new IllegalStateException("Failed to write relationships.", e);
            }
        });
        gen.writeEndArray();
    }

    protected void writeOptionalStringField(JsonGenerator gen, String fieldName, String fieldValue) {
        try {
            if (fieldValue != null) {
                gen.writeStringField(fieldName, fieldValue);
            }
        } catch (IOException e) {
            log.error("Failed to write field - " + fieldName, e);
            throw new IllegalStateException("Failed to write field - " + fieldName);
        }
    }
}
