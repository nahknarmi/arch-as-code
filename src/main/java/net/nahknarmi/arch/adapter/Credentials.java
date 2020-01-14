package net.nahknarmi.arch.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.vavr.control.Try;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class Credentials {
    private static final String STRUCTURIZR_PATH = ".arch-as-code" + File.separator + "structurizr";
    private static final String CREDENTIALS_FILE_PATH = STRUCTURIZR_PATH + File.separator + "credentials.json";

    static Optional<FileInputStream> credentialsAsStream() {
        return Try.of(() -> new FileInputStream(new File(CREDENTIALS_FILE_PATH)))
                .map(Optional::of)
                .getOrElse(Optional.empty());
    }

    public static void createCredentials(File productDocumentationRoot, String workspaceId, String apiKey, String apiSecret) throws IOException {
        String configPath = String.format("%s%s%s", productDocumentationRoot.getAbsolutePath(), File.separator, STRUCTURIZR_PATH);
        checkArgument(new File(configPath).mkdirs(), String.format("Unable to create directory %s.", configPath));

        File credentialsFile = new File(configPath + File.separator + "credentials.json");

        new ObjectMapper()
                .writeValue(credentialsFile,
                        ImmutableMap.of(
                                "workspace_id", workspaceId,
                                "api_key", apiKey,
                                "api_secret", apiSecret
                        )
                );
    }
}
