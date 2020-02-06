package net.nahknarmi.arch.commands;

import net.nahknarmi.arch.adapter.in.WorkspaceReader;
import net.nahknarmi.arch.adapter.out.WorkspaceWriter;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "import", description = "Imports existing struturizr workspace")
public class ImportCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", paramLabel = "EXPORTED_WORKSPACE", description = "Exported structurizr workspace location.", defaultValue = "./")
    private File exportedWorkspacePath;

    // Only for testing purposes
    public ImportCommand(File exportedWorkspacePath) {
        this.exportedWorkspacePath = exportedWorkspacePath;
    }

    public ImportCommand() {
    }

    @Override
    public Integer call() throws Exception {
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(this.exportedWorkspacePath);
        File exportedFile = new WorkspaceWriter().export(dataStructure);
        return new PublishCommand(exportedFile.getParentFile(), exportedFile.getName()).call();
    }
}
