package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.nahknarmi.arch.domain.c4.C4Container;

import java.io.IOException;

public class C4ContainerSerializer extends C4BaseEntitySerializer<C4Container> {
    public C4ContainerSerializer(Class<C4Container> t) {
        super(t);
    }

    @Override
    public void serialize(C4Container value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        baseEntityWrite(value, gen);

        gen.writeStringField("technology", value.getTechnology());
        gen.writeStringField("url", value.getUrl());

        gen.writeEndObject();
    }
}
