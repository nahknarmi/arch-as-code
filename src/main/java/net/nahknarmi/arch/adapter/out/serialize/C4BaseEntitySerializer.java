package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.BaseEntity;
import net.nahknarmi.arch.domain.c4.C4Action;
import net.nahknarmi.arch.domain.c4.C4Path;

import java.io.IOException;

abstract class C4BaseEntitySerializer<T extends BaseEntity> extends StdSerializer<T> {

    public C4BaseEntitySerializer(Class<T> t) {
        super(t);
    }

    protected void baseEntityWrite(T value, JsonGenerator gen) throws IOException {
        gen.writeStringField("path", value.getPath().getPath());
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
                //TODO, handle situation where technology is missing
                gen.writeStringField("technology", x.getTechnology());
                gen.writeEndObject();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        gen.writeEndArray();
    }


}
