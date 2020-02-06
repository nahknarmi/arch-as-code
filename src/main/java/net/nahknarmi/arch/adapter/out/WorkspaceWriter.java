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

public class WorkspaceWriter {
    private static final Log logger = LogFactory.getLog(WorkspaceWriter.class);

    public File export(ArchitectureDataStructure dataStructure) throws IOException {
        File tempFile = File.createTempFile("arch-as-code", ".yml");
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        SimpleModule module = new SimpleModule();
        module.addSerializer(new C4EntitySerializer(C4Person.class));
        module.addSerializer(new C4EntitySerializer(C4SoftwareSystem.class));
        module.addSerializer(new C4EntitySerializer(C4Container.class));
        module.addSerializer(new C4EntitySerializer(C4Component.class));
        module.addSerializer(new C4ViewSerializer(C4ContainerView.class));
        module.addSerializer(new C4ViewSerializer(C4ComponentView.class));
        module.addSerializer(new C4ViewSerializer(C4SystemView.class));
        objectMapper.registerModule(module);

        objectMapper.writeValue(tempFile, dataStructure);
        logger.info(String.format("Architecture data structure written to - %s", tempFile.getAbsolutePath()));

        return tempFile;
    }

}
