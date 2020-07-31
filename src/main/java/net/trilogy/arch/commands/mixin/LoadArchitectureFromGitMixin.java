package net.trilogy.arch.commands.mixin;

import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.commands.ParentCommand;
import net.trilogy.arch.domain.ArchitectureDataStructure;

import java.io.File;
import java.util.Optional;

public interface LoadArchitectureFromGitMixin extends DisplaysErrorMixin {

    File getProductArchitectureDirectory();

    GitInterface getGitInterface();

    default Optional<ArchitectureDataStructure> loadArchitectureFromGitOrPrintError(String gitReference, String errorMessageIfFailed) {
        final var productArchitecturePath = getProductArchitectureDirectory()
                .toPath()
                .resolve(ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME);
        try {
            return Optional.of(
                    getGitInterface().load(gitReference, productArchitecturePath)
            );
        } catch (final Exception e) {
            printError(errorMessageIfFailed, e);
            return Optional.empty();
        }
    }
}
