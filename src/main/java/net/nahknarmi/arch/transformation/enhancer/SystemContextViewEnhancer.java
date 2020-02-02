package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.Entity;
import net.nahknarmi.arch.domain.c4.view.C4SystemView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

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

    private void addEntities(List<C4Path> entities, ModelMediator modelMediator, SystemContextView view) {
        entities.forEach(addEntity(modelMediator, view));
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, SystemContextView view, C4SystemView s) {
        s.getTags()
                .forEach(tag ->
                        dataStructure
                                .getAllWithTag(tag)
                                .stream()
                                .map(Entity::getPath)
                                .forEach(addEntity(modelMediator, view))
                );
    }

    private Consumer<C4Path> addEntity(ModelMediator modelMediator, SystemContextView view) {
        return entityPath -> {
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
        };
    }
}
