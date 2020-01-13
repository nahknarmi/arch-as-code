package net.nahknarmi.arch.domain;

import net.nahknarmi.arch.transformation.validator.DataStructureValidationException;
import net.nahknarmi.arch.transformation.validator.ModelValidator;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ArchitectureDataStructureReader {

    public ArchitectureDataStructure load(File manifest) throws FileNotFoundException {
        checkNotNull(manifest, "Manifest must not be null.");
        checkArgument(manifest.exists(), String.format("Manifest file does not exist - %s.", manifest.getAbsolutePath()));

        ArchitectureDataStructure dataStructure = new Yaml().loadAs(new FileInputStream(manifest), ArchitectureDataStructure.class);
        List<String> messages = new ModelValidator().validate(dataStructure);
        if (!messages.isEmpty()) {
            messages.forEach(System.err::println);
            throw new DataStructureValidationException("Validation has failed.");
        }

        return dataStructure;
    }
}
