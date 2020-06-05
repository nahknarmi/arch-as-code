package net.trilogy.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.view.View;
import lombok.NonNull;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Reference;
import net.trilogy.arch.domain.c4.Entity;
import net.trilogy.arch.domain.c4.view.C4View;
import net.trilogy.arch.domain.c4.view.ModelMediator;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseViewEnhancer<T extends View, G extends C4View> implements WorkspaceEnhancer {

    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        @NonNull C4Model dataStructureModel = dataStructure.getModel();
        if (dataStructureModel.equals(C4Model.NONE)) {
            return;
        }
        List<G> c4Views = getViews(dataStructure);
        c4Views.forEach(c4View -> {
            T view = createView(workspace, dataStructureModel, c4View);

            ModelMediator modelMediator = new ModelMediator(workspace.getModel());
            addEntities(modelMediator, dataStructureModel, view, c4View);
            addTaggedEntities(modelMediator, dataStructure, view, c4View);

            view.setAutomaticLayout(true);
        });
    }

    public abstract List<G> getViews(ArchitectureDataStructure dataStructure);

    public abstract T createView(Workspace workspace, C4Model dataStructureModel, G c4View);

    public abstract Consumer<Entity> addEntity(ModelMediator modelMediator, C4Model dataStructureModel, T view);

    private void addEntities(ModelMediator modelMediator, C4Model dataStructureModel, T view, G c4View) {
        c4View.getElements()
                .stream()
                .map(viewReference -> viewReferenceToEntity(dataStructureModel, viewReference))
                .forEach(addEntity(modelMediator, dataStructureModel, view));
    }

    private Entity viewReferenceToEntity(C4Model dataStructureModel, C4Reference viewRef) {
        if (viewRef.getId() != null) {
            String id = viewRef.getId();
            return dataStructureModel.findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id));
        } else if (viewRef.getAlias() != null) {
            return dataStructureModel.findEntityByAlias(viewRef.getAlias());
        } else {
            throw new IllegalStateException("View reference missing both id and alias: " + viewRef);
        }
    }

    private void addTaggedEntities(ModelMediator modelMediator, ArchitectureDataStructure dataStructure, T context, G c4View) {
        c4View.getTags()
                .forEach(tag -> dataStructure.getAllWithTag(tag)
                        .stream()
                        .forEach(addEntity(modelMediator, dataStructure.getModel(), context)));
    }
}
