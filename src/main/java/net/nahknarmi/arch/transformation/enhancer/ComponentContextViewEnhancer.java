package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;

public class ComponentContextViewEnhancer implements WorkspaceEnhancer {


    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        List<C4ComponentView> componentViews = dataStructure.getViews().getComponentViews();
        componentViews.forEach(c -> {
            String systemName = c.getContainerPath().getSystemName();
            String containerName = c.getContainerPath().getContainerName()
                    .orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
            Model workspaceModel = workspace.getModel();
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
            Container container = softwareSystem.getContainerWithName(containerName);

            com.structurizr.view.ComponentView view = viewSet.createComponentView(container, c.getName(), c.getDescription());

            ModelMediator modelMediator = new ModelMediator(workspaceModel);
            addEntities(modelMediator, view, c);
            addTaggedEntities(modelMediator, dataStructure, c, view);

            view.setAutomaticLayout(true);
        });
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, C4ComponentView context, com.structurizr.view.ComponentView view) {
        context.getTags().forEach(tag -> {
            dataStructure.getAllWithTag(tag).forEach(tagable -> {
                if (tagable instanceof C4Person) {
                    view.add(modelMediator.person(((C4Person) tagable).getPath()));
                } else if (tagable instanceof C4SoftwareSystem) {
                    view.add(modelMediator.softwareSystem(((C4SoftwareSystem) tagable).getPath()));
                } else if (tagable instanceof C4Container) {
                    view.add(modelMediator.container(((C4Container) tagable).getPath()));
                } else if (tagable instanceof C4Component) {
                    view.add(modelMediator.component(((C4Component) tagable).getPath()));
                } else {
                    throw new IllegalStateException("Unsupported type " + tagable.getClass().getTypeName());
                }
            });
        });
    }

    private void addEntities(ModelMediator modelMediator, com.structurizr.view.ComponentView view, C4ComponentView componentView) {
        componentView.getEntities().forEach(entityPath -> {
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
        });
    }
}