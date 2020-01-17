package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Element;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ContainerView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.transformation.TransformationHelper;

public class ContainerContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        ViewSet viewSet = workspace.getViews();

        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }

        if (dataStructure.getModel().getViews().getContainerView() != null) {
            dataStructure.getModel().getViews().getContainerView().getContainers().forEach(c -> {
                String system = c.getSystem();
                SoftwareSystem softwareSystem = workspace.getModel().getSoftwareSystemWithName(system);

                //TODO: Deal with softwareSystem potentially being null
                Container container = softwareSystem.getContainerWithName(c.getName());
                ContainerView context = viewSet.createContainerView(softwareSystem, c.getName(), c.getDescription());

                // Now iterate through entities, in view, not relationships
//                c.getRelationships().forEach(r -> {
//                    String description = r.getDescription();
//                    Element fromElement = TransformationHelper.getElementWithName(workspace, c.getName());
//                    Element toElement = TransformationHelper.getElementWithName(workspace, r.getWith());
//
//                    addRelationshipToContext(context, fromElement, toElement, description);
//                });

                //TODO: deal with container potentially being null
                context.addNearestNeighbours(container);
                context.setAutomaticLayout(true);
            });
        }
    }


    private void addRelationshipToContext(ContainerView context, Element fromElement, Element toElement, String description) {
        // TODO: Clean up, currently only Person.uses & Container.uses interaction type supported
        if (fromElement instanceof Person) {
            if (toElement instanceof SoftwareSystem) {
                Person fromPerson = (Person) fromElement;
                SoftwareSystem toSystem = (SoftwareSystem) toElement;

                fromPerson.uses(toSystem, description);
                context.add(fromPerson);
                context.add(toSystem);
            } else {
                Person fromPerson = (Person) fromElement;
                Container toContainer = (Container) toElement;

                fromPerson.uses(toContainer, description);
                context.add(fromPerson);
                context.add(toContainer);
            }
        } else if (fromElement instanceof Container) {
            if (toElement instanceof SoftwareSystem) {
                Container fromContainer = (Container) fromElement;
                SoftwareSystem toSystem = (SoftwareSystem) toElement;

                fromContainer.uses(toSystem, description);
                context.add(fromContainer);
                context.add(toSystem);
            } else {
                Container fromContainer = (Container) fromElement;
                Container toContainer = (Container) toElement;

                fromContainer.uses(toContainer, description);
                context.add(fromContainer);
                context.add(toContainer);

            }
        }
    }
}
