package net.trilogy.arch.adapter.structurizr;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.trilogy.arch.adapter.structurizr.Credentials.config;

public class StructurizrAdapter {
    @Getter
    private StructurizrClient client;

    public StructurizrAdapter() {
    }

    public StructurizrAdapter(StructurizrClient client) {
        this.client = client;
    }

    /**
     * It will use following order to determine which workspace id to use:
     * - from environment variable
     * - from ./.arch-as-code/structurizr/credentials.json
     * - from workspace configured in passed in workspace
     *
     * @param workspace
     */
    public Boolean publish(Workspace workspace) {
        checkNotNull(workspace, "Workspace must not be null!");

        if (client == null) client = buildClient();

        try {
            client.putWorkspace(config().getWorkspaceId(), workspace);
        } catch (Exception e) {
            LogManager.getLogger(getClass()).error("Unable to publish to Structurizr", e);
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    StructurizrClient buildClient() {
        StructurizrClient client = new StructurizrClient(config().getApiKey(), config().getApiSecret());
        client.setWorkspaceArchiveLocation(null);
        return client;
    }

}
