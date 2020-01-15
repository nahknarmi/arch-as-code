package net.nahknarmi.arch.domain;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ArchitectureDataStructureReader {

    public ArchitectureDataStructure load(File manifest) throws FileNotFoundException {
        checkNotNull(manifest, "Manifest must not be null.");
        checkArgument(manifest.exists(), String.format("Manifest file does not exist - %s.", manifest.getAbsolutePath()));

        return new Yaml().loadAs(new FileInputStream(manifest), ArchitectureDataStructure.class);
    }
}
