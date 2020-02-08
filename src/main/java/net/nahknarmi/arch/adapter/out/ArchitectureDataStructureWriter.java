package net.nahknarmi.arch.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.nahknarmi.arch.adapter.out.serialize.C4EntitySerializer;
import net.nahknarmi.arch.adapter.out.serialize.C4ViewSerializer;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Component;
import net.nahknarmi.arch.domain.c4.C4Container;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;
import net.nahknarmi.arch.domain.c4.view.C4SystemView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public class ArchitectureDataStructureWriter {
    private static final Log logger = LogFactory.getLog(ArchitectureDataStructureWriter.class);

    public File export(ArchitectureDataStructure dataStructure) throws IOException {
        File tempFile = File.createTempFile("arch-as-code", ".yml");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.registerModule(buildModule());
        objectMapper.writeValue(tempFile, dataStructure);
        logger.info(String.format("Architecture data structure written to - %s", tempFile.getAbsolutePath()));

        return tempFile;
    }

    private SimpleModule buildModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new C4EntitySerializer(C4Person.class))
                .addSerializer(new C4EntitySerializer(C4SoftwareSystem.class))
                .addSerializer(new C4EntitySerializer(C4Container.class))
                .addSerializer(new C4EntitySerializer(C4Component.class))
                .addSerializer(new C4ViewSerializer(C4ContainerView.class))
                .addSerializer(new C4ViewSerializer(C4ComponentView.class))
                .addSerializer(new C4ViewSerializer(C4SystemView.class));
        return module;
    }

}
