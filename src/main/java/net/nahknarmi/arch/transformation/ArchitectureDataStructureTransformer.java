package net.nahknarmi.arch.transformation;

import com.structurizr.Workspace;
import net.nahknarmi.arch.adapter.WorkspaceConfigLoader;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.transformation.enhancer.WorkspaceEnhancer;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ArchitectureDataStructureTransformer {
    private final List<WorkspaceEnhancer> enhancers;
    private final WorkspaceConfigLoader workspaceConfigLoader;

    public ArchitectureDataStructureTransformer(List<WorkspaceEnhancer> enhancers, WorkspaceConfigLoader workspaceConfigLoader) {
        this.enhancers = enhancers;
        this.workspaceConfigLoader = workspaceConfigLoader;
    }

    public Workspace toWorkSpace(ArchitectureDataStructure dataStructure) {
        checkNotNull(dataStructure, "ArchitectureDataStructure must not be null.");

        Workspace workspace = new Workspace(dataStructure.getName(), dataStructure.getDescription());
        workspace.setId(workspaceConfigLoader.config().getWorkspaceId());

        this.enhancers.forEach(e -> e.enhance(workspace, dataStructure));
        return workspace;
    }
}
