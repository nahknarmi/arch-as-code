package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;

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
            String containerName = c.getContainerPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
            Model workspaceModel = workspace.getModel();
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
            Container container = softwareSystem.getContainerWithName(containerName);

            com.structurizr.view.ComponentView view = viewSet.createComponentView(container, c.getName(), c.getDescription());

            addEntities(workspaceModel, view, c);
            addTaggedEntities(workspaceModel, dataStructure, c, view);

            view.setAutomaticLayout(true);
        });
    }

    private void addTaggedEntities(Model workspaceModel, ArchitectureDataStructure dataStructure, C4ComponentView context, com.structurizr.view.ComponentView view) {
        context.getTags().forEach(tag -> {
            dataStructure.getAllWithTag(tag).forEach(tagable -> {
                if (tagable instanceof C4Person) {
                    String personName = ((C4Person) tagable).getPath().getPersonName();
                    Person person = workspaceModel.getPersonWithName(personName);
                    view.add(person);
                } else if (tagable instanceof C4SoftwareSystem) {
                    String systemName = ((C4SoftwareSystem) tagable).getPath().getSystemName();
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(systemName);
                    view.add(softwareSystemWithName);
                } else if (tagable instanceof C4Container) {
                    String systemName = ((C4SoftwareSystem) tagable).getPath().getSystemName();
                    String containerName = ((C4Component) tagable).getPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID missing!"));
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(systemName);
                    Container containerWithName = softwareSystemWithName.getContainerWithName(containerName);
                    view.add(containerWithName);
                } else if (tagable instanceof C4Component) {
                    String systemName = ((C4Component) tagable).getPath().getSystemName();
                    String containerName = ((C4Component) tagable).getPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID missing!"));
                    String componentName = ((C4Component) tagable).getPath().getComponentName().orElseThrow(() -> new IllegalStateException("Workspace ID missing!"));
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(systemName);
                    Container container = softwareSystemWithName.getContainerWithName(containerName);
                    Component componentWithName = container.getComponentWithName(componentName);
                    view.add(componentWithName);
                } else {
                    throw new IllegalStateException("Unsupported type " + tagable.getClass().getTypeName());
                }
            });
        });
    }

    private void addEntities(Model workspaceModel, com.structurizr.view.ComponentView view, C4ComponentView componentView) {
        componentView.getEntities().forEach(entityPath -> {
            switch (entityPath.getType()) {
                case person: {
                    String personName = entityPath.getPersonName();
                    Person person = workspaceModel.getPersonWithName(personName);
                    view.add(person);
                    break;
                }
                case system: {
                    String systemName = entityPath.getSystemName();
                    SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                    view.add(softwareSystem);
                    break;
                }
                case container: {
                    String systemName = entityPath.getSystemName();
                    String containerName = entityPath.getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                    SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                    Container container = softwareSystem.getContainerWithName(containerName);
                    view.add(container);
                    break;
                }
                case component: {
                    String systemName = entityPath.getSystemName();
                    String containerName = entityPath.getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                    String componentName = entityPath.getComponentName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                    SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                    Container container = softwareSystem.getContainerWithName(containerName);
                    Component component = container.getComponentWithName(componentName);
                    view.add(component);
                    break;
                }
                default:
                    throw new IllegalStateException("Unsupported type " + entityPath.getType());
            }
        });
    }
}
