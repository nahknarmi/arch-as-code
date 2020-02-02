package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.Entity;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public class ContainerContextViewEnhancer implements WorkspaceEnhancer {

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

            ModelMediator modelMediator = new ModelMediator(workspaceModel);
            addEntities(modelMediator, view, c);
            addTaggedEntities(modelMediator, dataStructure, view, c);

            view.setAutomaticLayout(true);
        });
    }

    private void addEntities(ModelMediator modelMediator, ContainerView view, C4ContainerView c) {
        c.getEntities().forEach(addEntity(modelMediator, view));
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, ContainerView context, C4ContainerView c) {
        c.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .stream()
                        .map(Entity::getPath)
                        .forEach(addEntity(modelMediator, context)));
    }

    private Consumer<C4Path> addEntity(ModelMediator modelMediator, ContainerView view) {
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
                default:
                    throw new IllegalStateException("Unsupported type " + entityPath.getType());
            }
        };
    }
}
