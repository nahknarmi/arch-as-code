package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.nahknarmi.arch.domain.c4.view.C4View;
import net.nahknarmi.arch.domain.c4.view.HasContainerPath;
import net.nahknarmi.arch.domain.c4.view.HasSystemPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.adapter.out.serialize.C4EntitySerializer.wrappedWriteStringField;

public class C4ViewSerializer<T extends C4View> extends StdSerializer<T> {
    private static final Log log = LogFactory.getLog(C4ViewSerializer.class);

    public C4ViewSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        baseViewSerialize(value, gen);
        writeContainerPath(value, gen);
        writeSystemPath(value, gen);

        gen.writeEndObject();
    }

    protected void baseViewSerialize(T value, JsonGenerator gen) throws IOException {
        gen.writeStringField("name", value.getName());
        gen.writeStringField("description", value.getDescription());

        writeTags(value, gen);
        writeReferences(value, gen);
    }

    private void writeReferences(T value, JsonGenerator gen) throws IOException {
        gen.writeFieldName("references");
        gen.writeStartArray();
        value.getEntities().forEach(path -> {
            try {
                gen.writeString(path.getPath());
            } catch (IOException e) {
                log.error("Failed to write references.", e);
                throw new IllegalStateException("Failed to references.", e);
            }
        });
        gen.writeEndArray();
    }

    private void writeTags(T value, JsonGenerator gen) throws IOException {
        gen.writeFieldName("tags");
        gen.writeStartArray();
        value.getTags().forEach(t -> {
            try {
                gen.writeString(t.getTag());
            } catch (IOException e) {
                log.error("Failed to write tags.", e);
                throw new IllegalStateException("Failed to tags.", e);
            }
        });
        gen.writeEndArray();
    }

    private void writeContainerPath(T value, JsonGenerator gen) {
        if (value instanceof HasContainerPath) {
            ofNullable(((HasContainerPath) value).getContainerPath())
                    .ifPresent((x) -> wrappedWriteStringField(gen, "containerPath", x.getPath()));
        }
    }

    private void writeSystemPath(T value, JsonGenerator gen) {
        if (value instanceof HasSystemPath) {
            ofNullable(((HasSystemPath) value).getSystemPath())
                    .ifPresent((x) -> wrappedWriteStringField(gen, "systemPath", x.getPath()));
        }
    }
}
