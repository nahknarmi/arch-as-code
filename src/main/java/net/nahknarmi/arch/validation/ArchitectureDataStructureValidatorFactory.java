package net.nahknarmi.arch.validation;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.domain.ArchitectureDataStructureReader;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;

public abstract class ArchitectureDataStructureValidatorFactory {

    public static ArchitectureDataStructureValidator create() {
        return new ArchitectureDataStructureValidator(
                ImmutableList.of(

                ),
                new ArchitectureDataStructureSchemaValidator(),
                new ArchitectureDataStructureReader()
        );
    }
}
