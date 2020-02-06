package net.nahknarmi.arch.adapter.out.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;

import java.io.IOException;

public class C4ContainerViewSerializer extends C4BaseViewSerializer<C4ContainerView> {

    public C4ContainerViewSerializer(Class<C4ContainerView> t) {
        super(t);
    }

    @Override
    public void serialize(C4ContainerView value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        baseViewSerialize(value, gen);

        gen.writeStringField("systemPath", value.getSystemPath().getPath());

        gen.writeEndObject();
    }

}
