package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.view.C4ContainerView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public class ContainerContextViewEnhancer extends BaseViewEnhancer<ContainerView, C4ContainerView> {

    @Override
    public List<C4ContainerView> getViews(ArchitectureDataStructure dataStructure) {
        return dataStructure.getViews().getContainerViews();
    }

    @Override
    public ContainerView createView(Workspace workspace, C4ContainerView c) {
        ViewSet viewSet = workspace.getViews();

        SoftwareSystem softwareSystem = new ModelMediator(workspace.getModel()).softwareSystem(c.getSystemPath());
        return viewSet.createContainerView(softwareSystem, c.getKey(), c.getDescription());
    }

    public Consumer<C4Path> addEntity(ModelMediator modelMediator, ContainerView view) {
        return entityPath -> {

            switch (entityPath.type()) {
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
                    throw new IllegalStateException("Unsupported type " + entityPath.type());
            }
        };
    }
}
