package net.trilogy.arch.adapter.structurizr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public static void createCredentials(
            File productArchitectureDirectory,
            String workspaceId,
            String apiKey,
            String apiSecret) throws IOException {
        final var configPath = format("%s%s%s",
                productArchitectureDirectory.getAbsolutePath(),
                File.separator,
                STRUCTURIZR_PATH);
        if (!new File(configPath).mkdirs())
            throw new IllegalArgumentException(format("Unable to create directory %s.", configPath));

        final var credentialsFile = new File(configPath + File.separator + "credentials.json");

        new ObjectMapper().writeValue(credentialsFile, ImmutableMap.of(
                "workspace_id", workspaceId,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    static FileInputStream credentialsAsStream() {
        try {
            return new FileInputStream(CREDENTIALS_FILE_PATH);
        } catch (final FileNotFoundException e) {
            return null;
        }
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

        return value == null ? details().get(jsonKey) : null;
    }

    @SuppressWarnings("unchecked")
    static Map<String, String> details() {
        final var credentialsFile = credentialsAsStream();
        if (null == credentialsFile) return emptyMap();
        // TODO: Use Jackson not Gson
        //       Note that the javadoc for Gson calls out: "this method should
        //       not be used if the desired type is a generic type"
        //       Jackson sensibly throws IOException -- how to manage?
        //       Gson itself throws under the same conditions, but throws
        //       classes which extend `RuntimeException`
        return (Map<String, String>) new Gson().fromJson(new InputStreamReader(credentialsFile), Map.class);
    }
}
