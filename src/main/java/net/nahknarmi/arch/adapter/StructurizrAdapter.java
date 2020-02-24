package net.nahknarmi.arch.adapter;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;

import static com.google.common.base.Preconditions.checkNotNull;

public class StructurizrAdapter {
    private final WorkspaceConfigLoader workspaceConfigLoader;

    public StructurizrAdapter(WorkspaceConfigLoader workspaceConfigLoader) {
        this.workspaceConfigLoader = workspaceConfigLoader;
    }

    public Workspace load(long workspaceId) throws StructurizrClientException {
        StructurizrClient buildClient = buildClient();
        return buildClient.getWorkspace(workspaceId);
    }

    /**
     * It will use following order to determine which workspace id to use:
     *  - from environment variable
     *  - from ./.arch-as-code/structurizr/credentials.json
     *  - from workspace configured in passed in workspace
     * @param workspace
     * @throws StructurizrClientException
     */
    public void publish(Workspace workspace) throws StructurizrClientException {
        checkNotNull(workspace, "Workspace must not be null!");
        WorkspaceConfig config = workspaceConfigLoader.config();
        buildClient().putWorkspace(config.getWorkspaceId(), workspace);
    }

    @SuppressWarnings("unchecked")
    private StructurizrClient buildClient() {
        WorkspaceConfig config = workspaceConfigLoader.config();

        StructurizrClient result = new StructurizrClient(config.getApiKey(), config.getApiSecret());
        result.setWorkspaceArchiveLocation(null);

        return result;
    }

}
