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

import java.util.List;

public class SystemContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        List<C4SystemView> systemViews = dataStructure.getModel().getViews().getSystemViews();
        systemViews.forEach(systemView -> {
            Model workspaceModel = workspace.getModel();
            String systemName = systemView.getSystemPath().getSystemName();
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
            SystemContextView view = viewSet.createSystemContextView(softwareSystem, systemName, systemView.getDescription());

            addEntities(systemView.getEntities(), workspaceModel, view);
            addTaggedEntities(workspaceModel, dataStructure, view, systemView);

            view.setAutomaticLayout(true);
        });
    }

    private void addTaggedEntities(Model workspaceModel, ArchitectureDataStructure dataStructure, SystemContextView view, C4SystemView s) {
        s.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .forEach(tagable -> {
                            if (tagable instanceof C4Person) {
                                String personName = ((C4Person) tagable).getName();
                                Person person = workspaceModel.getPersonWithName(personName);
                                view.add(person);
                            } else if (tagable instanceof C4SoftwareSystem) {
                                String systemName = ((C4SoftwareSystem) tagable).getName();
                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(systemName);
                                view.add(system);
                            }
                        }));
    }

    private void addEntities(List<C4Path> entities, Model workspaceModel, SystemContextView view) {
        entities.forEach(e -> {
            addElementToSystemView(workspaceModel, view, e);
        });
    }

    private void addElementToSystemView(Model workspaceModel, SystemContextView view, C4Path entityPath) {
        switch (entityPath.getType()) {
            case person:
                Person person = workspaceModel.getPersonWithName(entityPath.getPersonName());
                view.add(person);
                break;
            case system:
                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(entityPath.getSystemName());
                view.add(system);
                break;
            default:
                throw new IllegalStateException("Unsupported relationship type " + entityPath.getType());
        }
    }
}
