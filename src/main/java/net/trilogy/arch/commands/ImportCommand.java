package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.adapter.structurizr.WorkspaceReader;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.concurrent.Callable;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureWriter.exportArchitectureDataStructure;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureWriter.writeViews;

@Command(name = "import", mixinStandardHelpOptions = true, description = "Imports existing structurizr workspace, overwriting the existing product architecture.")
public class ImportCommand implements Callable<Integer>, DisplaysOutputMixin, DisplaysErrorMixin {
    private final FilesFacade filesFacade;
    @Parameters(index = "0", paramLabel = "EXPORTED_WORKSPACE", description = "Exported structurizr workspace json file location.")
    private File exportedWorkspacePath;
    @Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;
    @Getter
    @Spec
    private CommandLine.Model.CommandSpec spec;

    public ImportCommand(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    @Override
    public Integer call() {
        logArgs();

        try {
            final var dataStructure = new WorkspaceReader().load(exportedWorkspacePath);
            final var writeFile = productArchitectureDirectory.toPath().resolve(ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME).toFile();
            final var exportedFile = exportArchitectureDataStructure(dataStructure, writeFile, filesFacade);
            print(String.format("Architecture data structure written to - %s", exportedFile.getAbsolutePath()));

            final var workspaceViews = new WorkspaceReader().loadViews(exportedWorkspacePath);
            final var viewsPath = writeViews(workspaceViews, productArchitectureDirectory.toPath(), filesFacade);
            print(String.format("Views were written to - %s", viewsPath));
        } catch (Exception e) {
            printError("Failed to import", e);
            return 1;
        }

        return 0;
    }
}
