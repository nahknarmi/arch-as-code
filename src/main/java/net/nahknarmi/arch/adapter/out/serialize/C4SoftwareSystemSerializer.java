package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.nahknarmi.arch.domain.c4.C4Location;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;

import java.io.IOException;

import static java.util.Optional.ofNullable;

public class C4SoftwareSystemSerializer extends C4BaseEntitySerializer<C4SoftwareSystem> {
    public C4SoftwareSystemSerializer(Class<C4SoftwareSystem> t) {
        super(t);
    }

    @Override
    public void serialize(C4SoftwareSystem value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("location", ofNullable(value.getLocation()).orElse(C4Location.UNSPECIFIED).name());

        baseEntityWrite(value, gen);

        gen.writeEndObject();
    }
}
