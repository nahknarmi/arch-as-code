package net.nahknarmi.arch.adapter;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static net.nahknarmi.arch.adapter.Credentials.credentialsAsStream;

public class WorkspaceConfigLoader {
    private Map<String, String> details;

    public WorkspaceConfig config() {
        checkArgument(workspaceId().isPresent(), "Workspace id missing. Check config.");
        checkArgument(apiKey().isPresent(), "Structurizr api key missing. Check config.");
        checkArgument(apiSecret().isPresent(), "Structurizr api secret missing. Check config.");

        return WorkspaceConfig.builder()
                .apiKey(apiKey().get())
                .apiSecret(apiSecret().get())
                .workspaceId(workspaceId().get())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Optional<Long> workspaceId() {
        return readWorkspaceDetail("STRUCTURIZR_WORKSPACE_ID", "workspace_id")
                .map(Long::parseLong);
    }

    private Optional<String> apiKey() {
        return readWorkspaceDetail("STRUCTURIZR_API_KEY", "api_key");
    }

    private Optional<String> apiSecret() {
        return readWorkspaceDetail("STRUCTURIZR_API_SECRET", "api_secret");
    }

    private Optional<String> readWorkspaceDetail(String environmentVariableName, String jsonKey) {
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
    private Map<String, String> details() {
        if (details == null) {
            details = credentialsAsStream()
                    .map(InputStreamReader::new)
                    .map(x -> new Gson().fromJson(x, Map.class))
                    .orElse(emptyMap());
        }
        return details;
    }
}
