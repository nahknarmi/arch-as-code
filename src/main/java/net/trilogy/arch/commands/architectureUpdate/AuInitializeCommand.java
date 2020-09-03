package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.JIRA_API_SETTINGS_FILE_PATH;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_GOOGLE_API_AUTH_PROVIDER_CERT_URL;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_GOOGLE_API_AUTH_URI;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_GOOGLE_API_REDIRECT_URI;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_GOOGLE_API_REDIRECT_URN;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_GOOGLE_API_TOKEN_URI;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_JIRA_BASE_URI;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_JIRA_BULK_CREATE_ENDPOINT;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_JIRA_GET_STORY_ENDPOINT;
import static net.trilogy.arch.commands.architectureUpdate.AuInitializeConstants.INITIAL_JIRA_LINK_PREFIX;

@Command(name = "initialize", aliases = "init", mixinStandardHelpOptions = true, description = "Initialize the architecture updates work space within a single product's existing workspace. Sets up Google API credentials to import P1 documents.")
public class AuInitializeCommand implements Callable<Integer>, DisplaysOutputMixin, DisplaysErrorMixin {
    private final FilesFacade filesFacade;

    @Option(names = {"-c", "--client-id"}, description = "Google API client id", required = true)
    private String googleApiClientId;

    @Option(names = {"-p", "--project-id"}, description = "Google API project id", required = true)
    private String googleApiProjectId;

    @Option(names = {"-s", "--secret"}, description = "Google API secret", required = true)
    private String googleApiSecret;

    @Parameters(index = "0", description = "Product workspace directory, containng the product's architecture")
    private File productArchitectureDirectory;

    @Getter
    @Spec
    private CommandLine.Model.CommandSpec spec;

    public AuInitializeCommand(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    @Override
    public Integer call() {
        logArgs();
        if (!makeAuFolder()) return 1;
        if (!makeJiraSettingsFile()) return 1;
        if (!makeGoogleApiCredentialsFolder()) return 1;
        if (!createGoogleApiClientCredentialsFile(googleApiClientId, googleApiProjectId, googleApiSecret))
            return 1;

        print(String.format("Architecture updates initialized under - %s", productArchitectureDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATES_ROOT_FOLDER).toFile()));
        return 0;
    }

    private boolean makeJiraSettingsFile() {
        File file = productArchitectureDirectory.toPath().resolve(JIRA_API_SETTINGS_FILE_PATH).toFile();
        if (!file.getParentFile().mkdirs()) return false;
        try {
            filesFacade.writeString(file.toPath(), buildJiraSettingsJsonString());
            return true;
        } catch (IOException e) {
            printError(String.format("Unable to create %s", file.getAbsolutePath()));
            return false;
        }
    }

    private boolean createGoogleApiClientCredentialsFile(String clientId, String projectId, String secret) {
        File file = productArchitectureDirectory.toPath().resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).resolve(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME).toFile();
        String credentialJsonString = buildCredentialJsonString(clientId, projectId, secret);
        try {
            filesFacade.writeString(file.toPath(), credentialJsonString);
            return true;
        } catch (IOException e) {
            print(String.format("Unable to create %s", file.getAbsolutePath()));
            return false;
        }
    }

    private String buildJiraSettingsJsonString() {
        return "{\n" +
                "    \"base_uri\": \"" + INITIAL_JIRA_BASE_URI + "\",\n" +
                "    \"link_prefix\": \"" + INITIAL_JIRA_LINK_PREFIX + "\",\n" +
                "    \"get_story_endpoint\": \"" + INITIAL_JIRA_GET_STORY_ENDPOINT + "\",\n" +
                "    \"bulk_create_endpoint\": \"" + INITIAL_JIRA_BULK_CREATE_ENDPOINT + "\"\n" +
                "}";
    }

    private String buildCredentialJsonString(String clientId, String projectId, String secret) {
        return "{\n" +
                "  \"installed\": {\n" +
                "    \"client_id\": \"" + clientId.strip() + "\",\n" +
                "    \"project_id\": \"" + projectId.strip() + "\",\n" +
                "    \"auth_uri\": \"" + INITIAL_GOOGLE_API_AUTH_URI + "\",\n" +
                "    \"token_uri\": \"" + INITIAL_GOOGLE_API_TOKEN_URI + "\",\n" +
                "    \"auth_provider_x509_cert_url\": \"" + INITIAL_GOOGLE_API_AUTH_PROVIDER_CERT_URL + "\",\n" +
                "    \"client_secret\": \"" + secret.strip() + "\",\n" +
                "    \"redirect_uris\": [\n" +
                "      \"" + INITIAL_GOOGLE_API_REDIRECT_URN + "\",\n" +
                "      \"" + INITIAL_GOOGLE_API_REDIRECT_URI + "\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

    private boolean makeAuFolder() {
        File auFolder = productArchitectureDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATES_ROOT_FOLDER).toFile();

        boolean succeeded = true;
        if (Files.exists(auFolder.toPath())) {
            print(String.format("Architecture Updates directory %s already exists", auFolder.getAbsolutePath()));
        } else {
            succeeded = auFolder.mkdir();
        }

        if (!succeeded) {
            printError(String.format("Unable to create %s", auFolder.getAbsolutePath()));
            return false;
        }

        return true;
    }

    private boolean makeGoogleApiCredentialsFolder() {
        File auCredentialFolder = productArchitectureDirectory.toPath()
                .resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).toFile();

        boolean credSucceeded = auCredentialFolder.mkdirs();
        if (!credSucceeded) {
            printError(String.format("Unable to create %s", auCredentialFolder.getAbsolutePath()));
            return false;
        }
        return true;
    }
}
