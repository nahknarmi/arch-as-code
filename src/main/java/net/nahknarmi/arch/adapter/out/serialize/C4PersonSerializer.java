package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.nahknarmi.arch.domain.c4.C4Person;

import java.io.IOException;

public class C4PersonSerializer extends C4BaseEntitySerializer<C4Person> {
    public C4PersonSerializer(Class<C4Person> t) {
        super(t);
    }

    @Override
    public void serialize(C4Person value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        baseEntityWrite(value, gen);

        gen.writeStringField("location", value.getLocation().name());

        gen.writeEndObject();
    }
}
