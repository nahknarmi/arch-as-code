package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.ComponentContext;

public class ComponentContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        C4ComponentView componentView = dataStructure.getModel().getViews().getComponentView();
        if (componentView != null) {
            componentView.getComponents().forEach(c -> {
                String systemName = c.getContainerPath().getSystemName();
                String containerName = c.getContainerPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                Model workspaceModel = workspace.getModel();
                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                Container container = softwareSystem.getContainerWithName(containerName);

                ComponentView view = viewSet.createComponentView(container, c.getName(), c.getDescription());

                addEntities(workspaceModel, softwareSystem, view, c);
                addTaggedEntities(workspaceModel, dataStructure, c, view);

                view.setAutomaticLayout(true);

            });
        }
    }

    private void addTaggedEntities(Model workspaceModel, ArchitectureDataStructure dataStructure, ComponentContext context, ComponentView view) {
        context.getTags().forEach(x -> {
            dataStructure.getAllWithTag(x).forEach(t -> {
                if (t instanceof C4Person) {
                    Person personWithName = workspaceModel.getPersonWithName(((C4Person) t).getPath().getName());
                    view.add(personWithName);
                } else if (t instanceof C4SoftwareSystem) {
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(((C4SoftwareSystem) t).getPath().getSystemName());
                    view.add(softwareSystemWithName);
                } else if (t instanceof C4Container) {
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(((C4SoftwareSystem) t).getPath().getSystemName());
                    Container containerWithName = softwareSystemWithName.getContainerWithName(((C4Container) t).getName());
                    view.add(containerWithName);
                } else if (t instanceof C4Component) {
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName((((C4Component) t).getPath().getSystemName()));
                    Container containerWithName = softwareSystemWithName.getContainerWithName(((C4Component) t).getPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID missing!")));
                    Component componentWithName = containerWithName.getComponentWithName(((C4Component) t).getName());
                    view.add(componentWithName);
                } else {
                    throw new IllegalStateException("Unsupported type " + t.getClass().getTypeName());
                }
            });
        });
    }

    private void addEntities(Model workspaceModel, SoftwareSystem softwareSystem, ComponentView view, ComponentContext componentContext) {
        componentContext.getEntities().forEach(x -> {
            switch (x.getType()) {
                case person:
                    Person personWithName = workspaceModel.getPersonWithName(x.getName());
                    view.add(personWithName);
                    break;
                case system:
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(x.getName());
                    view.add(softwareSystemWithName);
                    break;
                case container:
                    SoftwareSystem softwareSystemWithName1 = workspaceModel.getSoftwareSystemWithName(x.getSystemName());
                    Container containerWithName = softwareSystemWithName1.getContainerWithName(x.getName());
                    view.add(containerWithName);
                    break;
                case component:
                    SoftwareSystem softwareSystemWithName2 = workspaceModel.getSoftwareSystemWithName(x.getSystemName());
                    Container containerWithName2 = softwareSystemWithName2.getContainerWithName(x.getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!")));
                    Component componentWithName = containerWithName2.getComponentWithName(x.getName());
                    view.add(componentWithName);
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + x.getType());
            }
        });
    }
}
