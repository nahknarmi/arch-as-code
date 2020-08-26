package net.trilogy.arch.commands;

import com.structurizr.view.ViewSet;
import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureWriter;
import net.trilogy.arch.adapter.structurizr.WorkspaceReader;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "import", mixinStandardHelpOptions = true, description = "Imports existing structurizr workspace, overwriting the existing product architecture.")
public class ImportCommand implements Callable<Integer>, DisplaysOutputMixin, DisplaysErrorMixin {

    @CommandLine.Parameters(index = "0", paramLabel = "EXPORTED_WORKSPACE", description = "Exported structurizr workspace json file location.")
    private File exportedWorkspacePath;

    @CommandLine.Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @Getter
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private final FilesFacade filesFacade;

    public ImportCommand(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    @Override
    public Integer call() {
        logArgs();

        try {
            ArchitectureDataStructure dataStructure = new WorkspaceReader().load(this.exportedWorkspacePath);
            File writeFile = this.productArchitectureDirectory.toPath().resolve(ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME).toFile();
            ArchitectureDataStructureWriter architectureDataStructureWriter = new ArchitectureDataStructureWriter(filesFacade);
            File exportedFile = architectureDataStructureWriter.export(dataStructure, writeFile);
            print(String.format("Architecture data structure written to - %s", exportedFile.getAbsolutePath()));

            ViewSet workspaceViews = new WorkspaceReader().loadViews(this.exportedWorkspacePath);
            Path viewsPath = architectureDataStructureWriter.writeViews(workspaceViews, productArchitectureDirectory.toPath());
            print(String.format("Views were written to - %s", viewsPath));

        } catch (Exception e) {
            printError("Failed to import", e);
            return 1;
        }

        return 0;
    }
}
