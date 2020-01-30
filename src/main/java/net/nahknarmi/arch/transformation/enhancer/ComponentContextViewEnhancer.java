package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;

public class ComponentContextViewEnhancer implements WorkspaceEnhancer {

    private final ModelMediator modelMediator = new ModelMediator();

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
                    String containerName = ((C4Component) tagable).getPath().getContainerName()
                            .orElseThrow(() -> new IllegalStateException("Workspace ID missing!"));
                    SoftwareSystem softwareSystemWithName = workspaceModel.getSoftwareSystemWithName(systemName);
                    Container containerWithName = softwareSystemWithName.getContainerWithName(containerName);
                    view.add(containerWithName);
                } else if (tagable instanceof C4Component) {
                    String systemName = ((C4Component) tagable).getPath().getSystemName();
                    String containerName = ((C4Component) tagable).getPath().getContainerName()
                            .orElseThrow(() -> new IllegalStateException("Workspace ID missing!"));
                    String componentName = ((C4Component) tagable).getPath().getComponentName()
                            .orElseThrow(() -> new IllegalStateException("Workspace ID missing!"));
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
                case person:
                    view.add(modelMediator.person(entityPath, workspaceModel));
                    break;
                case system:
                    view.add(modelMediator.system(entityPath, workspaceModel));
                    break;
                case container:
                    view.add(modelMediator.container(entityPath, workspaceModel));
                    break;
                case component:
                    view.add(modelMediator.component(entityPath, workspaceModel));
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entityPath.getType());
            }
        });
    }
}