package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.view.C4SystemView;
import net.nahknarmi.arch.domain.c4.view.SystemContext;

import java.util.List;

public class SystemContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        C4SystemView systemView = dataStructure.getModel().getViews().getSystemView();
        if (systemView != null) {
            systemView.getSystems().forEach(systemContext -> {
                Model workspaceModel = workspace.getModel();
                String systemName = systemContext.getSystemPath().getSystemName();
                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                SystemContextView context = viewSet.createSystemContextView(softwareSystem, systemName, systemContext.getDescription());

                addEntities(systemContext.getEntities(), workspaceModel, context);
                addTaggedEntities(dataStructure, systemContext, workspaceModel, context);

                context.setAutomaticLayout(true);
            });
        }
    }

    private void addTaggedEntities(ArchitectureDataStructure dataStructure, SystemContext s, Model workspaceModel, SystemContextView context) {
        s.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .forEach(x -> {
                            if (x instanceof C4Person) {
                                Person person = workspaceModel.getPersonWithName(((C4Person) x).getName());
                                context.add(person);
                            } else if (x instanceof C4SoftwareSystem) {
                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(((C4SoftwareSystem) x).getName());
                                context.add(system);
                            }
                        }));
    }

    private void addEntities(List<C4Path> entities, Model workspaceModel, SystemContextView context) {
        entities.forEach(e -> {
            addElementToSystemContext(workspaceModel, context, e);
        });
    }

    private void addElementToSystemContext(Model workspaceModel, SystemContextView context, C4Path e) {
        switch (e.getType()) {
            case person:
                Person person = workspaceModel.getPersonWithName(e.getPersonName());
                context.add(person);
                break;
            case system:
                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(e.getSystemName());
                context.add(system);
                break;
            default:
                throw new IllegalStateException("Unsupported relationship type " + e.getType());
        }
    }
}
