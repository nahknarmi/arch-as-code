package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;

public class ContainerContextViewEnhancer implements WorkspaceEnhancer {

    private final ModelMediator modelMediator = new ModelMediator();

    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        ViewSet viewSet = workspace.getViews();
        List<C4ContainerView> containerViews = dataStructure.getViews().getContainerViews();
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
                                context.add(modelMediator.person(((C4Person) tagable).getPath(), workspaceModel));
                            } else if (tagable instanceof C4SoftwareSystem) {
                                context.add(modelMediator.system(((C4SoftwareSystem) tagable).getPath(), workspaceModel));
                            } else if (tagable instanceof C4Container) {
                                context.add(modelMediator.container(((C4Container) tagable).getPath(), workspaceModel));
                            }
                        }));
    }

    private void addElementToContainerView(Model workspaceModel, ContainerView view, C4Path path) {
        switch (path.getType()) {
            case person:
                view.add(modelMediator.person(path, workspaceModel));
                break;
            case system:
                view.add(modelMediator.system(path, workspaceModel));
                break;
            case container:
                view.add(modelMediator.container(path, workspaceModel));
                break;
            default:
                throw new IllegalStateException("Unsupported relationship type " + path.getType());
        }
    }
}
