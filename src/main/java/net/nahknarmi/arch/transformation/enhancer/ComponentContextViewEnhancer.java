package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.Entity;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public class ComponentContextViewEnhancer implements WorkspaceEnhancer {


    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        List<C4ComponentView> componentViews = dataStructure.getViews().getComponentViews();
        componentViews.forEach(componentView -> {
            String systemName = componentView.getContainerPath().getSystemName();
            String containerName = componentView.getContainerPath().getContainerName()
                    .orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
            Model workspaceModel = workspace.getModel();
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
            Container container = softwareSystem.getContainerWithName(containerName);

            com.structurizr.view.ComponentView view = viewSet.createComponentView(container, componentView.getName(), componentView.getDescription());

            ModelMediator modelMediator = new ModelMediator(workspaceModel);
            addEntities(modelMediator, view, componentView);
            addTaggedEntities(modelMediator, dataStructure, componentView, view);

            view.setAutomaticLayout(true);
        });
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, C4ComponentView context, ComponentView view) {
        context.getTags()
                .forEach(tag ->
                        dataStructure.getAllWithTag(tag)
                                .stream()
                                .map(Entity::getPath)
                                .forEach(addEntity(modelMediator, view)));
    }

    private void addEntities(ModelMediator modelMediator, ComponentView view, C4ComponentView componentView) {
        componentView.getEntities().forEach(addEntity(modelMediator, view));
    }

    private Consumer<C4Path> addEntity(ModelMediator modelMediator, ComponentView view) {
        return entityPath -> {

            switch (entityPath.getType()) {
                case person:
                    view.add(modelMediator.person(entityPath));
                    break;
                case system:
                    view.add(modelMediator.softwareSystem(entityPath));
                    break;
                case container:
                    view.add(modelMediator.container(entityPath));
                    break;
                case component:
                    view.add(modelMediator.component(entityPath));
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entityPath.getType());
            }
        };
    }


}