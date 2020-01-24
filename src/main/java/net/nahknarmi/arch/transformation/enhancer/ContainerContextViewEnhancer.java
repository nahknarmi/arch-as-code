package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;
import net.nahknarmi.arch.domain.c4.view.ContainerContext;

public class ContainerContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        C4ContainerView containerView = dataStructure.getModel().getViews().getContainerView();
        if (containerView != null) {
            containerView.getContainers().forEach(c -> {
                String systemName = c.getPath().getSystemName();
                Model workspaceModel = workspace.getModel();
                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);

                ContainerView view = viewSet.createContainerView(softwareSystem, c.getPath().getName(), c.getDescription());

                addEntities(workspaceModel, softwareSystem, view, c);
                addTaggedEntities(workspaceModel, dataStructure, c, view);

                view.setAutomaticLayout(true);
            });
        }
    }

    private void addEntities(Model workspaceModel, SoftwareSystem softwareSystem, ContainerView view, ContainerContext c) {
        c.getEntities().forEach(x -> addElementToSystemContext(workspaceModel, softwareSystem, view, x));
    }

    private void addTaggedEntities(Model workspaceModel, ArchitectureDataStructure dataStructure, ContainerContext c, ContainerView context) {
        c.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .forEach(t -> {
                            if (t instanceof C4Person) {
                                Person person = workspaceModel.getPersonWithName(((C4Person) t).getName());
                                context.add(person);
                            } else if (t instanceof C4SoftwareSystem) {
                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(((C4SoftwareSystem) t).getName());
                                context.add(system);
                            } else if (t instanceof C4Container) {
                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(c.getPath().getSystemName());
                                Container container = system.getContainerWithName(c.getPath().getName());
                                context.add(container);
                            }
                        }));
    }


    private void addElementToSystemContext(Model workspaceModel, SoftwareSystem softwareSystem, ContainerView view, C4Path path) {
        switch (path.getType()) {
            case person:
                Person person = workspaceModel.getPersonWithName(path.getName());
                view.add(person);
                break;
            case system:
                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(path.getName());
                view.add(system);
                break;
            case container:
                Container container = softwareSystem.getContainerWithName(path.getName());
                view.add(container);
                break;
            default:
                throw new IllegalStateException("Unsupported relationship type " + path.getType());
        }
    }


}
