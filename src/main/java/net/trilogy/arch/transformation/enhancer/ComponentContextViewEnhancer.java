package net.trilogy.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ViewSet;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Container;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.Entity;
import net.trilogy.arch.domain.c4.view.C4ComponentView;
import net.trilogy.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public class ComponentContextViewEnhancer extends BaseViewEnhancer<ComponentView, C4ComponentView> {
    @Override
    public List<C4ComponentView> getViews(ArchitectureDataStructure dataStructure) {
        return dataStructure.getViews().getComponentViews();
    }

    @Override
    public ComponentView createView(Workspace workspace, C4Model dataStructureModel, C4ComponentView componentView) {
        ViewSet viewSet = workspace.getViews();
        C4Container cont = componentView.getReferenced(dataStructureModel);
        Container container = new ModelMediator(workspace.getModel()).container(cont.getId());

        return viewSet.createComponentView(container, componentView.getKey(), componentView.getDescription());
    }

    public Consumer<Entity> addEntity(ModelMediator modelMediator, C4Model dataStructureModel, ComponentView view) {
        return entity -> {

            switch (entity.getType()) {
                case person:
                    view.add(modelMediator.person(entity.getId()));
                    break;
                case system:
                    view.add(modelMediator.softwareSystem(entity.getId()));
                    break;
                case container:
                    view.add(modelMediator.container(entity.getId()));
                    break;
                case component:
                    C4Component comp = (C4Component) entity;
                    String containerId;
                    if (comp.getContainerId() != null) {
                        String id = comp.getContainerId();
                        containerId = dataStructureModel.findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id)).getId();
                    } else if (comp.getContainerAlias() != null) {
                        containerId = dataStructureModel.findEntityByAlias(comp.getContainerAlias()).getId();
                    } else {
                        throw new IllegalStateException("Component missing both containerID and containerAlias: " + comp);
                    }

                    if (view.getContainer().getId().equals(containerId)) {
                        view.add(modelMediator.component(entity.getId()));
                    } else {
                        System.err.println(String.format("Component:\n %s\n is not a member of the\n %s\n container and cannot be added to it's view.", comp, view.getContainer()));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entity.getType());
            }
        };
    }
}
