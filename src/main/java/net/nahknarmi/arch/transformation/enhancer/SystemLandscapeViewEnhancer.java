package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.view.SystemLandscapeView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;

public class SystemLandscapeViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        ViewSet viewSet = workspace.getViews();

        SystemLandscapeView systemLandscapeView = viewSet.createSystemLandscapeView("SystemLandscape", "The system landscape diagram for the entire org");
        systemLandscapeView.addAllElements();
        systemLandscapeView.setAutomaticLayout(true);
    }
}
