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

import java.util.List;

public class ContainerContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        List<C4ContainerView> containerViews = dataStructure.getModel().getViews().getContainerViews();
        containerViews.forEach(c -> {
            String systemName = c.getSystemPath().getSystemName();
            Model workspaceModel = workspace.getModel();
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);

            ContainerView view = viewSet.createContainerView(softwareSystem, c.getName(), c.getDescription());

            addEntities(workspaceModel, view, c);
            addTaggedEntities(workspaceModel, dataStructure, view, c);

            view.setAutomaticLayout(true);
        });
    }

    private void addEntities(Model workspaceModel, ContainerView view, C4ContainerView c) {
        c.getEntities().forEach(x -> addElementToContainerView(workspaceModel, view, x));
    }

    private void addTaggedEntities(Model workspaceModel, ArchitectureDataStructure dataStructure, ContainerView context, C4ContainerView c) {
        c.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .forEach(tagable -> {
                            if (tagable instanceof C4Person) {
                                String personName = ((C4Person) tagable).getName();
                                Person person = workspaceModel.getPersonWithName(personName);
                                context.add(person);
                            } else if (tagable instanceof C4SoftwareSystem) {
                                String systemName = ((C4SoftwareSystem) tagable).getName();
                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(systemName);
                                context.add(system);
                            } else if (tagable instanceof C4Container) {
                                String systemName = ((C4Container) tagable).getPath().getSystemName();
                                String containerName = ((C4Container) tagable).getPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(systemName);
                                Container container = system.getContainerWithName(containerName);
                                context.add(container);
                            }
                        }));
    }

    private void addElementToContainerView(Model workspaceModel, ContainerView view, C4Path path) {
        switch (path.getType()) {
            case person: {
                Person person = workspaceModel.getPersonWithName(path.getPersonName());
                view.add(person);
                break;
            }
            case system: {
                SoftwareSystem system = workspaceModel.getSoftwareSystemWithName(path.getSystemName());
                view.add(system);
                break;
            }
            case container: {
                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(path.getSystemName());
                String containerName = path.getContainerName().orElseThrow(() -> new IllegalStateException("WorkSpace ID is missing!"));
                Container container = softwareSystem.getContainerWithName(containerName);
                view.add(container);
                break;
            }
            default:
                throw new IllegalStateException("Unsupported relationship type " + path.getType());
        }
    }
}
