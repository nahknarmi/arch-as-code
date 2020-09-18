package net.trilogy.arch.adapter.structurizr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public final class StructurizrCredentials {
    private static final String STRUCTURIZR_PATH = ".arch-as-code" + File.separator + "structurizr";
    private static final String CREDENTIALS_FILE_PATH = STRUCTURIZR_PATH + File.separator + "credentials.json";

    private static final String WORKSPACE_ID_ENV_VAR_NAME = "STRUCTURIZR_WORKSPACE_ID";
    private static final String API_KEY_ENV_VAR_NAME = "STRUCTURIZR_API_KEY";
    private static final String API_SECRET_ENV_VAR_NAME = "STRUCTURIZR_API_SECRET";

    public static WorkspaceConfig config() {
        return WorkspaceConfig.builder()
                .apiKey(requireNonNull(apiKey(), "Structurizr api key missing. Check config."))
                .apiSecret(requireNonNull(apiSecret(), "Structurizr api key missing. Check config."))
                .workspaceId(requireNonNull(workspaceId(), "Workspace id missing. Check config."))
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

    static Long workspaceId() {
        final var workspaceId = readWorkspaceDetail(WORKSPACE_ID_ENV_VAR_NAME, "workspace_id");
        return null == workspaceId ? null : Long.valueOf(workspaceId);
    }

    static String apiKey() {
        return readWorkspaceDetail(API_KEY_ENV_VAR_NAME, "api_key");
    }

    static String apiSecret() {
        return readWorkspaceDetail(API_SECRET_ENV_VAR_NAME, "api_secret");
    }

    static String readWorkspaceDetail(String environmentVariableName, String jsonKey) {
        final var value = getenv().get(environmentVariableName);

        return value != null ? value : details().get(jsonKey);
    }

    static Map<String, String> details() {
        try {
            return new ObjectMapper().readValue(new File(CREDENTIALS_FILE_PATH), new TypeReference<>() {
            });
        } catch (final IOException e) {
            // TODO: This *swallows* lots of problems we should be telling the user about
            return emptyMap();
        }
    }
}
