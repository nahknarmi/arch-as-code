package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.view.C4SystemView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;

public class SystemContextViewEnhancer implements WorkspaceEnhancer {

    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        List<C4SystemView> systemViews = dataStructure.getViews().getSystemViews();
        systemViews.forEach(systemView -> {
            Model workspaceModel = workspace.getModel();
            String systemName = systemView.getSystemPath().getSystemName();
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
            SystemContextView view = viewSet.createSystemContextView(softwareSystem, systemName, systemView.getDescription());

            ModelMediator modelMediator = new ModelMediator(workspaceModel);
            addEntities(systemView.getEntities(), modelMediator, view);
            addTaggedEntities(modelMediator, dataStructure, view, systemView);

            view.setAutomaticLayout(true);
        });
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, SystemContextView view, C4SystemView s) {
        s.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .forEach(tagable -> {
                            if (tagable instanceof C4Person) {
                                view.add(modelMediator.person(((C4Person) tagable).getPath()));
                            } else if (tagable instanceof C4SoftwareSystem) {
                                view.add(modelMediator.softwareSystem(((C4SoftwareSystem) tagable).getPath()));
                            }
                        }));
    }

    private void addEntities(List<C4Path> entities, ModelMediator modelMediator, SystemContextView view) {
        entities.forEach(e -> {
            addElementToSystemView(modelMediator, view, e);
        });
    }

    private void addElementToSystemView(ModelMediator modelMediator, SystemContextView view, C4Path entityPath) {
        switch (entityPath.getType()) {
            case person:
                view.add(modelMediator.person(entityPath));
                break;
            case system:
                view.add(modelMediator.softwareSystem(entityPath));
                break;
            default:
                throw new IllegalStateException("Unsupported relationship type " + entityPath.getType());
        }
    }
}
