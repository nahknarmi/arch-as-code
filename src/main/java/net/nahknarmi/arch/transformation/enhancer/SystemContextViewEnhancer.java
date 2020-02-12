package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.view.C4SystemView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public class SystemContextViewEnhancer extends BaseViewEnhancer<SystemContextView, C4SystemView> {

    @Override
    public List<C4SystemView> getViews(ArchitectureDataStructure dataStructure) {
        return dataStructure.getViews().getSystemViews();
    }

    @Override
    public SystemContextView createView(Workspace workspace, C4SystemView view) {
        ViewSet viewSet = workspace.getViews();
        SoftwareSystem softwareSystem = new ModelMediator(workspace.getModel()).softwareSystem(view.getSystemPath());
        return viewSet.createSystemContextView(softwareSystem, view.getKey(), view.getDescription());
    }

    public Consumer<C4Path> addEntity(ModelMediator modelMediator, SystemContextView view) {
        return entityPath -> {
            switch (entityPath.type()) {
                case person:
                    view.add(modelMediator.person(entityPath));
                    break;
                case system:
                    view.add(modelMediator.softwareSystem(entityPath));
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entityPath.type());
            }
        };
    }
}
