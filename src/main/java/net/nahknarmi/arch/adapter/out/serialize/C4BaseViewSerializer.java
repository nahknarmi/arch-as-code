package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.nahknarmi.arch.domain.c4.view.C4View;

import java.io.IOException;

public abstract class C4BaseViewSerializer<T extends C4View> extends StdSerializer<T> {
    public C4BaseViewSerializer(Class<T> t) {
        super(t);
    }

    protected void baseViewSerialize(T value, JsonGenerator gen) throws IOException {
        gen.writeStringField("name", value.getName());
        gen.writeStringField("description", value.getDescription());

        gen.writeFieldName("tags");
        gen.writeStartArray();
        value.getTags().forEach(t -> {
            try {
                gen.writeString(t.getTag());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        gen.writeEndArray();

        gen.writeFieldName("references");
        gen.writeStartArray();
        value.getEntities().forEach(path -> {
            try {
                gen.writeString(path.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        gen.writeEndArray();
    }
}
