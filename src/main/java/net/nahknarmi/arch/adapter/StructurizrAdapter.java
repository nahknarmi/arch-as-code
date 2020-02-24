package net.nahknarmi.arch.adapter;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import com.structurizr.api.StructurizrClientException;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.nahknarmi.arch.adapter.Credentials.config;

public class StructurizrAdapter {

    public StructurizrAdapter() {
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
        buildClient().putWorkspace(config().getWorkspaceId(), workspace);
    }

    @SuppressWarnings("unchecked")
    private StructurizrClient buildClient() {
        StructurizrClient result = new StructurizrClient(config().getApiKey(), config().getApiSecret());
        result.setWorkspaceArchiveLocation(null);
        return result;
    }

}
