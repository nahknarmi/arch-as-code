    package net.trilogy.arch.adapter.structurizr;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.trilogy.arch.adapter.structurizr.StructurizrCredentials.config;

@RequiredArgsConstructor
public class StructurizrAdapter {
    @Getter
    private final StructurizrClient client;

    public StructurizrAdapter() {
        this(buildClient());
    }

    /**
     * It will use following order to determine which workspace id to use: -
     * from environment variable - from ./.arch-as-code/structurizr/credentials.json
     * - from workspace configured in passed in workspace
     */
    public Boolean publish(Workspace workspace) {
        checkNotNull(workspace, "Workspace must not be null!");

        try {
            client.setMergeFromRemote(false);
            client.putWorkspace(config().getWorkspaceId(), workspace);

            return true;
        } catch (Exception e) {
            LogManager.getLogger(getClass()).error("Unable to publish to Structurizr", e);

            return false;
        }
    }

    private static StructurizrClient buildClient() {
        final var client = new StructurizrClient(config().getApiKey(), config().getApiSecret());
        client.setWorkspaceArchiveLocation(null);
        return client;
    }
}
