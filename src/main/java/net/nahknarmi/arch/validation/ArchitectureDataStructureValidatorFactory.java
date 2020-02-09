package net.nahknarmi.arch.validation;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.adapter.in.ArchitectureDataStructureReader;
import net.nahknarmi.arch.schema.ArchitectureDataStructureSchemaValidator;

public abstract class ArchitectureDataStructureValidatorFactory {

    public static ArchitectureDataStructureValidator create() {
        return new ArchitectureDataStructureValidator(
                ImmutableList.of(
                    new ModelValidator()
                ),
                new ArchitectureDataStructureSchemaValidator(),
                new ArchitectureDataStructureReader()
        );
    }
}
