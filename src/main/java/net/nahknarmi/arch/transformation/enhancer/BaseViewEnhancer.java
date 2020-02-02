package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.view.View;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.Entity;
import net.nahknarmi.arch.domain.c4.view.C4View;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseViewEnhancer<T extends View, G extends C4View> implements WorkspaceEnhancer {

    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }
        List<G> c4Views = getViews(dataStructure);
        c4Views.forEach(c4View -> {
            T view = createView(workspace, c4View);

            ModelMediator modelMediator = new ModelMediator(workspace.getModel());
            addEntities(modelMediator, view, c4View);
            addTaggedEntities(modelMediator, dataStructure, view, c4View);

            view.setAutomaticLayout(true);
        });
    }

    public abstract List<G> getViews(ArchitectureDataStructure dataStructure);

    public abstract T createView(Workspace workspace, G c4View);

    public abstract Consumer<C4Path> addEntity(ModelMediator modelMediator, T view);

    private void addEntities(ModelMediator modelMediator, T view, G c4View) {
        c4View.getEntities().forEach(addEntity(modelMediator, view));
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, T context, G c4View) {
        c4View.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .stream()
                        .map(Entity::getPath)
                        .forEach(addEntity(modelMediator, context)));
    }
}
