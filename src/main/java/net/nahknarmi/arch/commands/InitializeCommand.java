package net.nahknarmi.arch.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static net.nahknarmi.arch.adapter.Credentials.createCredentials;

@CommandLine.Command(name = "init", description = "Initializes project")
public class InitializeCommand implements Callable<Integer> {
    private static final Log logger = LogFactory.getLog(InitializeCommand.class);

    @CommandLine.Option(names = {"-i", "--workspace-id"}, description = "Structurizr workspace id", required = true)
    private String workspaceId;

    @CommandLine.Option(names = {"-k", "--workspace-api-key"}, description = "Structurizr workspace api key", required = true)
    private String apiKey;

    @CommandLine.Option(names = {"-s", "--workspace-api-secret"}, description = "Structurizr workspace api secret", required = true)
    private String apiSecret;

    @CommandLine.Parameters(description = "Product documentation root directory")
    private File productDocumentationRoot;

    // for testing purposes
    public InitializeCommand(String workspaceId, String apiKey, String apiSecret, File productDocumentationRoot) {
        this.workspaceId = workspaceId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.productDocumentationRoot = productDocumentationRoot;
    }

    public InitializeCommand() {

    }

    @Override
    public Integer call() throws Exception {
        logger.info(String.format("Architecture as code initialized under - %s\n", productDocumentationRoot.getAbsolutePath()));

        createCredentials(productDocumentationRoot, workspaceId, apiKey, apiSecret);
        createManifest();

        logger.info("You're ready to go!!");

        return 0;
    }

    private void createManifest() throws IOException {
        File manifestFile = new File(productDocumentationRoot.getAbsolutePath() + File.separator + "data-structure.yml");

        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();
        dataStructure.setDescription("Architecture as code");
        dataStructure.setName("Hello World!!!");
        dataStructure.setBusinessUnit("DevFactory");

        Path targetManifestPath = Paths.get(manifestFile.toURI());

        new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .writeValue(targetManifestPath.toFile(), dataStructure);

        logger.info(String.format("Manifest file written to - %s", manifestFile.getAbsolutePath()));
    }
}
