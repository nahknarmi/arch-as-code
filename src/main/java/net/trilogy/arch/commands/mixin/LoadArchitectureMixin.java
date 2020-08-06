package net.trilogy.arch.commands.mixin;

import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.commands.ParentCommand;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.util.Optional;

public interface LoadArchitectureMixin extends DisplaysErrorMixin {

    File getProductArchitectureDirectory();

    ArchitectureDataStructureObjectMapper getArchitectureDataStructureObjectMapper();

    FilesFacade getFilesFacade();

    default Optional<ArchitectureDataStructure> loadArchitectureOrPrintError(String errorMessageIfFailed) {
        final var productArchitecturePath = getProductArchitectureDirectory()
                .toPath()
                .resolve(ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME);

        try {
            return Optional.of(
                    getArchitectureDataStructureObjectMapper().readValue(
                            getFilesFacade().readString(productArchitecturePath)
                    )
            );
        } catch (final Exception e) {
            printError(errorMessageIfFailed, e);
            return Optional.empty();
        }
    }
}
