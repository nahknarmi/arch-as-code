package net.nahknarmi.arch.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static net.nahknarmi.arch.adapter.Credentials.createCredentials;

@CommandLine.Command(name = "init", description = "Initializes project")
public class InitializeCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-i", "--workspace-id"}, description = "Structurizr workspace id", required = true)
    String workspaceId;

    @CommandLine.Option(names = {"-k", "--workspace-api-key"}, description = "Structurizr workspace api key", required = true)
    String apiKey;

    @CommandLine.Option(names = {"-s", "--workspace-api-secret"}, description = "Structurizr workspace api secret", required = true)
    String apiSecret;

    @CommandLine.Parameters(description = "Product documentation root directory", defaultValue = "./")
    File productDocumentationRoot;

    @Override
    public Integer call() throws Exception {
        System.out.println(String.format("Architecture as code initialized under - %s\n", productDocumentationRoot.getAbsolutePath()));

        createCredentials(productDocumentationRoot, workspaceId, apiKey, apiSecret);
        createManifest();

        //create documentation directory with sample file

        System.out.println("\nYou're ready to go!!");

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

        System.out.println(String.format("Manifest file written to - %s", manifestFile.getAbsolutePath()));
    }
}
