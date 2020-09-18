package net.trilogy.arch.commands;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureWriter.exportArchitectureDataStructure;
import static net.trilogy.arch.adapter.structurizr.StructurizrCredentials.createCredentials;

@Command(name = "init", description = "Initializes a new workspace directory to contain a single project architecture, AUs, documentation, and credentials for Structurizr imports and exports. This is generally the first command to be run.", mixinStandardHelpOptions = true)
@RequiredArgsConstructor
public class InitializeCommand implements Callable<Integer>, DisplaysOutputMixin, DisplaysErrorMixin {
    private final FilesFacade filesFacade;

    @Option(names = {"-i", "--workspace-id"}, description = "Structurizr workspace id", required = true)
    private String workspaceId;
    @Option(names = {"-k", "--workspace-api-key"}, description = "Structurizr workspace api key", required = true)
    private String apiKey;
    @Option(names = {"-s", "--workspace-api-secret"}, description = "Structurizr workspace api secret", required = true)
    private String apiSecret;
    @Parameters(index = "0", description = "Directory to initialize")
    private File productArchitectureDirectory;
    @Getter
    @Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() {
        logArgs();

        try {
            // TODO: Creating credentials is *messed up* -- produces nonsense credentials
            // createCredentials(productArchitectureDirectory, workspaceId, apiKey, apiSecret);
            createManifest();
            print(format("Architecture as code initialized under - %s", productArchitectureDirectory.getAbsolutePath()));
            print("You're ready to go!!");

            return 0;
        } catch (Exception e) {
            printError("Unable to initialize", e);
        }

        return 1;
    }

    private void createManifest() throws IOException {
        ArchitectureDataStructure data = createSampleDataStructure();
        String toFilePath = productArchitectureDirectory.getAbsolutePath() + File.separator + ParentCommand.PRODUCT_ARCHITECTURE_FILE_NAME;
        write(data, toFilePath);
        print("Manifest file written to - " + toFilePath);
    }

    private void write(ArchitectureDataStructure data, String toFilePath) throws IOException {
        File manifestFile = new File(toFilePath);
        exportArchitectureDataStructure(data, manifestFile, filesFacade);
    }

    private ArchitectureDataStructure createSampleDataStructure() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();
        dataStructure.setDescription("Architecture as code");
        dataStructure.setName("Hello World!!!");
        dataStructure.setBusinessUnit("DevFactory");
        return dataStructure;
    }
}
