package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.nahknarmi.arch.domain.c4.C4Component;

import java.io.IOException;

import static java.util.Optional.ofNullable;

public class C4ComponentSerializer extends C4BaseEntitySerializer<C4Component> {

    public C4ComponentSerializer(Class<C4Component> t) {
        super(t);
    }

    @Override
    public void serialize(C4Component value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        baseEntityWrite(value, gen);

        ofNullable(value.getTechnology()).ifPresent((x) -> writeOptionalStringField(gen, "technology", x));
        ofNullable(value.getUrl()).ifPresent((x) -> writeOptionalStringField(gen, "url", x));

        gen.writeEndObject();
    }

}
