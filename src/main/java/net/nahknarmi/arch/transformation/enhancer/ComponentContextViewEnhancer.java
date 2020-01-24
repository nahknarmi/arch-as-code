package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Element;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;

public class ComponentContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        ViewSet viewSet = workspace.getViews();

        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }

        if (dataStructure.getModel().getViews().getComponentView() != null) {
            dataStructure.getModel().getViews().getComponentView().getComponents().forEach(c -> {
//                String containerName = c.getContainer();
//                Container container = findContainerByName(workspace, containerName);

//                ComponentView context = viewSet.createComponentView(container, c.getName(), c.getDescription());

                  // Now iterate through entities, in view, not relationships
//                c.getRelationships().forEach(r -> {
//                    String description = r.getDescription();
//                    Element fromElement = TransformationHelper.getElementWithName(workspace, c.getName());
//                    Element toElement = TransformationHelper.getElementWithName(workspace, r.getWith());
//
//                    addRelationshipToContext(context, fromElement, toElement, description);
//                });

//                context.addNearestNeighbours(container);
//                context.setAutomaticLayout(true);
            });
        }
    }

    private Container findContainerByName(Workspace workspace, String containerName) {
        return (Container) workspace.getModel()
                .getElements()
                .stream()
                .filter(e -> e.getName().equals(containerName) && e instanceof Container)
                .findFirst()
                .get();
    }

    private void addRelationshipToContext(ComponentView context, Element fromElement, Element toElement, String description) {
        // TODO: Currently only Component.uses interaction type supported
        Component fromComponent = (Component) fromElement;
        Component toComponent = (Component) toElement;

        fromComponent.uses(toComponent, description);
        context.add(fromComponent);
        context.add(toComponent);
    }
}
