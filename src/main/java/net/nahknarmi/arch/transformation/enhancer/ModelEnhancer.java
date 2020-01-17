package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Model.NONE;

public class ModelEnhancer implements WorkspaceEnhancer {

    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        Model model = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();
        addPeople(model, dataStructureModel);
        addSystems(model, dataStructureModel);
    }

    private void addPeople(Model model, C4Model dataStructureModel) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getPeople()
                .forEach(p -> model.addPerson(p.getName(), p.getDescription()));
    }

    private void addSystems(Model model, C4Model dataStructureModel) {
        ofNullable(dataStructureModel)
                .orElse(NONE).getSystems()
                .forEach(s -> {
                    addSystem(model, s);
                });
    }

    private void addSystem(Model model, C4SoftwareSystem s) {
        SoftwareSystem softwareSystem = model.addSoftwareSystem(s.getName(), s.getDescription());
        s.getContainers().forEach(c -> {
            addContainer(softwareSystem, c);
        });
    }

    private void addContainer(SoftwareSystem softwareSystem, C4Container c) {
        String containerName = c.getName();
        softwareSystem.addContainer(containerName, c.getDescription(), c.getTechnology());
        Container container = softwareSystem.getContainerWithName(containerName);

        c.getComponents().forEach(comp -> {
            addComponent(container, comp);
        });
    }

    private void addComponent(Container container, C4Component c) {
        container.addComponent(c.getName(), c.getDescription(), c.getTechnology());
    }
}
