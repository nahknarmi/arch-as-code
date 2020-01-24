package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Container;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;
import net.nahknarmi.arch.domain.c4.view.C4EntityReference;
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
//                String s = c.getSystem();
                Model workspaceModel = workspace.getModel();
//                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(s);

//                ContainerView context = viewSet.createContainerView(softwareSystem, c.getName(), c.getDescription());

//                addEntities(workspaceModel, softwareSystem, context, c);
//                addTaggedEntities(workspaceModel, dataStructure, c, context);

//                context.setAutomaticLayout(true);
            });
        }
    }

    private void addEntities(Model workspaceModel, SoftwareSystem softwareSystem, ContainerView context, ContainerContext c) {
//        c.getEntities().forEach(x -> addElementToSystemContext(workspaceModel, softwareSystem, context, x));
    }

    private void addTaggedEntities(Model workspaceModel, ArchitectureDataStructure dataStructure, ContainerContext c, ContainerView context) {
        c.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .forEach(t -> {
                            if (t instanceof C4Person) {
//                                Person person = workspaceModel.getPersonWithName(((C4Person) t).getName());
//                                context.add(person);
                            } else if (t instanceof C4SoftwareSystem) {
//                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(((C4SoftwareSystem) t).getName());
//                                context.add(system);
                            } else if (t instanceof C4Container) {
//                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(((C4Container) t).getName());
//                                Container container = system.getContainerWithName(c.getName());
//                                context.add(container);
                            }
                        }));
    }


    private void addElementToSystemContext(Model workspaceModel, SoftwareSystem softwareSystem, ContainerView context, C4EntityReference entityReference) {
        switch (entityReference.getType()) {
            case person:
                Person person = workspaceModel.getPersonWithName(entityReference.getName());
                context.add(person);
                break;
            case system:
                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(entityReference.getName());
                context.add(system);
                break;
            case container:
                Container container = softwareSystem.getContainerWithName(entityReference.getName());
                context.add(container);
                break;
            default:
                throw new IllegalStateException("Unsupported relationship type " + entityReference.getType());
        }
    }


}
