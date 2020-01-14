package net.nahknarmi.arch.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkArgument;

@CommandLine.Command(name = "init", description = "Initializes project")
public class InitializeCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-w", "--workspace-id"}, description = "Structurizr workspace id", required = true)
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

        createCredentials();
        createManifest();

        //create documentation directory with sample file

        System.out.println("\nYou're ready to go!!");

        return 0;
    }

    private void createManifest() throws IOException {
        File manifestFile = new File(productDocumentationRoot.getAbsolutePath() + File.separator + "data-structure.yml");
        checkArgument(manifestFile.createNewFile(), String.format("Manifest file %s already exists.", manifestFile.getAbsolutePath()));

        Files.write(Paths.get(manifestFile.toURI()), "name: my-awesome-product\ndescription: arch as code".getBytes());

        System.out.println(String.format("Manifest file written to - %s", manifestFile.getAbsolutePath()));
    }

    //TODO: move this to credentials file reader
    private void createCredentials() throws IOException {
        String configPath = String.format("%s%s.arch-as-code%s%s", productDocumentationRoot, File.separator, File.separator, "structurizr");
        checkArgument(new File(configPath).mkdirs(), String.format("Unable to create directory %s", configPath));

        File credentialsFile = new File(configPath + File.separator + "credentials.json");

        new ObjectMapper()
                .writeValue(credentialsFile,
                        ImmutableMap.of(
                                "workspace_id", workspaceId,
                                "api_key", apiKey,
                                "api_secret", apiSecret
                        )
                );
        System.out.println(String.format("Credentials written to - %s", credentialsFile.getAbsolutePath()));
    }
}
