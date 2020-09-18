package net.trilogy.arch.adapter.structurizr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.vavr.control.Try;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;

public abstract class StructurizrCredentials {
    private static final String STRUCTURIZR_PATH = ".arch-as-code" + File.separator + "structurizr";
    private static final String CREDENTIALS_FILE_PATH = STRUCTURIZR_PATH + File.separator + "credentials.json";

    private static final String WORKSPACE_ID_ENV_VAR_NAME = "STRUCTURIZR_WORKSPACE_ID";
    private static final String API_KEY_ENV_VAR_NAME = "STRUCTURIZR_API_KEY";
    private static final String API_SECRET_ENV_VAR_NAME = "STRUCTURIZR_API_SECRET";

    public static WorkspaceConfig config() {
        checkArgument(workspaceId().isPresent(), "Workspace id missing. Check config.");
        checkArgument(apiKey().isPresent(), "Structurizr api key missing. Check config.");
        checkArgument(apiSecret().isPresent(), "Structurizr api secret missing. Check config.");

        return WorkspaceConfig.builder()
                .apiKey(apiKey().get())
                .apiSecret(apiSecret().get())
                .workspaceId(workspaceId().get())
                .build();
    }

    // TODO [TESTING]: Add unit tests
    // TODO: DO NOT OVERWRITE EXISTING CREDENTIALS
    public static void createCredentials(
            File productArchitectureDirectory,
            String workspaceId,
            String apiKey,
            String apiSecret) throws IOException {
        String configPath = format("%s%s%s",
                productArchitectureDirectory.getAbsolutePath(),
                File.separator,
                STRUCTURIZR_PATH);
        final var dirs = new File(configPath);
        //noinspection ResultOfMethodCallIgnored
        dirs.mkdirs();
        if (!dirs.exists()) {
            throw new IllegalStateException("Directory created, but doesn't exist afterwards: " + dirs);
        }

        final var credentialsFile = new File(configPath + File.separator + "credentials.json");

        new ObjectMapper().writeValue(credentialsFile, Map.of(
                "workspace_id", workspaceId,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    static Optional<FileInputStream> credentialsAsStream() {
        return Try.of(() -> new FileInputStream(new File(CREDENTIALS_FILE_PATH)))
                .map(Optional::of)
                .getOrElse(empty());
    }

    static Optional<Long> workspaceId() {
        return readWorkspaceDetail(WORKSPACE_ID_ENV_VAR_NAME, "workspace_id")
                .map(Long::parseLong);
    }

    static Optional<String> apiKey() {
        return readWorkspaceDetail(API_KEY_ENV_VAR_NAME, "api_key");
    }

    static Optional<String> apiSecret() {
        return readWorkspaceDetail(API_SECRET_ENV_VAR_NAME, "api_secret");
    }

    static Optional<String> readWorkspaceDetail(String environmentVariableName, String jsonKey) {
        String value = System.getenv().get(environmentVariableName);
        if (value != null) {
            return Optional.of(value);
        } else if (!details().isEmpty()) {
            return Optional.of(details().get(jsonKey));
        } else {
            return empty();
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, String> details() {
        return credentialsAsStream()
                .map(InputStreamReader::new)
                .map(x -> new Gson().fromJson(x, Map.class))
                .orElse(emptyMap());
    }
}
