package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ViewSet;
import lombok.NonNull;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public class ComponentContextViewEnhancer extends BaseViewEnhancer<ComponentView, C4ComponentView> {

    @Override
    public List<C4ComponentView> getViews(ArchitectureDataStructure dataStructure) {
        return dataStructure.getViews().getComponentViews();
    }

    @Override
    public ComponentView createView(Workspace workspace, C4ComponentView componentView) {
        ViewSet viewSet = workspace.getViews();
        @NonNull C4Path containerPath = componentView.getContainerPath();
        String systemName = containerPath.getSystemName();
        String containerName = containerPath.getContainerName()
                .orElseThrow(() -> new IllegalStateException("Container name not found in path " + containerPath));
        Model workspaceModel = workspace.getModel();
        SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
        Container container = softwareSystem.getContainerWithName(containerName);

        return viewSet.createComponentView(container, componentView.getName(), componentView.getDescription());
    }

    public Consumer<C4Path> addEntity(ModelMediator modelMediator, ComponentView view) {
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