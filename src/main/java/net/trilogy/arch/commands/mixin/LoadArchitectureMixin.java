package net.trilogy.arch.commands.mixin;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.util.Optional;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.commands.ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME;

public interface LoadArchitectureMixin extends DisplaysErrorMixin {
    File getProductArchitectureDirectory();

    FilesFacade getFilesFacade();

    default Optional<ArchitectureDataStructure> loadArchitectureOrPrintError(String errorMessageIfFailed) {
        final var productArchitecturePath = getProductArchitectureDirectory()
                .toPath()
                .resolve(PRODUCT_ARCHITECTURE_FILE_NAME);

        try {
            return Optional.of(YAML_OBJECT_MAPPER.readValue(
                    getFilesFacade().readString(productArchitecturePath), ArchitectureDataStructure.class));
        } catch (final Exception e) {
            printError(errorMessageIfFailed, e);
            return Optional.empty();
        }
    }
}
