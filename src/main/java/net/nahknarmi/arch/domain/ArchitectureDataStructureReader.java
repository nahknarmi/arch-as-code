package net.nahknarmi.arch.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ArchitectureDataStructureReader {

    public ArchitectureDataStructure load(File manifest) throws IOException {
        checkNotNull(manifest, "Manifest must not be null.");
        checkArgument(manifest.exists(), String.format("Manifest file does not exist - %s.", manifest.getAbsolutePath()));


        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(new FileInputStream(manifest), ArchitectureDataStructure.class);


//        return new Yaml().loadAs();
    }
}
