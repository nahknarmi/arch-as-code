package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Element;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.transformation.TransformationHelper;

public class SystemContextViewEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        ViewSet viewSet = workspace.getViews();

        if (dataStructure.getModel().equals(C4Model.NONE)) {
            return;
        }

        if (dataStructure.getModel().getViews().getSystemView() != null) {
            dataStructure.getModel().getViews().getSystemView().getSystems().forEach(s -> {
                SoftwareSystem softwareSystem = workspace.getModel().getSoftwareSystemWithName(s.getName());

                //TODO: deal with softwareSystem potentially being null!!
                SystemContextView context = viewSet.createSystemContextView(softwareSystem, s.getName(), s.getDescription());

                // Now iterate through entities, in view, not relationships
//                s.getRelationships().forEach(r -> {
//                    String description = r.getDescription();
//                    Element fromElement = TransformationHelper.getElementWithName(workspace, s.getName());
//                    Element toElement = TransformationHelper.getElementWithName(workspace, r.getWith());
//
//                    addRelationshipToContext(context, fromElement, toElement, description);
//                });

                context.setAutomaticLayout(true);
            });
        }
    }

    private void addRelationshipToContext(SystemContextView context, Element fromElement, Element toElement, String description) {
        // TODO: Clean up, currently only Person.uses interaction type supported
        if (fromElement instanceof Person) {
            Person fromPerson = (Person) fromElement;
            SoftwareSystem toSystem = (SoftwareSystem) toElement;

            fromPerson.uses(toSystem, description);
            context.add(fromPerson);
            context.add(toSystem);
        }
    }
}
