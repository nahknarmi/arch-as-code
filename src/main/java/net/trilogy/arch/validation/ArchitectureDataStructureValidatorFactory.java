package net.trilogy.arch.validation;

import com.google.common.collect.ImmutableList;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureReader;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.schema.SchemaValidator;

public abstract class ArchitectureDataStructureValidatorFactory {
    @SuppressWarnings("deprecation")
    public static ArchitectureDataStructureValidator create() {
        return new ArchitectureDataStructureValidator(
                ImmutableList.of(
                        new ModelValidator(),
                        new ModelReferenceValidator(),
                        new RelationsValidator()
                ),
                new SchemaValidator(),
                new ArchitectureDataStructureReader(new FilesFacade())
        );
    }
}
