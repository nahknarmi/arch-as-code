package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Model.NONE;

public class ModelEnhancer implements WorkspaceEnhancer {

    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        Model workspaceModel = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();
        addPeople(workspaceModel, dataStructureModel);
        addSystems(workspaceModel, dataStructureModel);
        addRelationships(workspaceModel, dataStructureModel);
    }

    private void addPeople(Model model, C4Model dataStructureModel) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getPeople()
                .forEach(p -> model.addPerson(p.getName(), p.getDescription()));
    }

    private void addSystems(Model model, C4Model dataStructureModel) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getSystems()
                .forEach(s -> addSystem(model, s));
    }

    private void addSystem(Model model, C4SoftwareSystem s) {
        SoftwareSystem softwareSystem = model.addSoftwareSystem(s.getName(), s.getDescription());
        s.getContainers().forEach(c -> addContainer(softwareSystem, c));
    }

    private void addContainer(SoftwareSystem softwareSystem, C4Container c) {
        String containerName = c.getName();
        softwareSystem.addContainer(containerName, c.getDescription(), c.getTechnology());
        Container container = softwareSystem.getContainerWithName(containerName);

        c.getComponents().forEach(comp -> addComponent(container, comp));
    }

    private void addComponent(Container container, C4Component c) {
        container.addComponent(c.getName(), c.getDescription(), c.getTechnology());
    }

    private void addRelationships(Model workspaceModel, C4Model dataStructureModel) {
        addPeopleRelationships(workspaceModel, dataStructureModel);
        addSystemRelationships(workspaceModel, dataStructureModel);
        addContainerRelationships(workspaceModel, dataStructureModel);
        addComponentRelationships(workspaceModel, dataStructureModel);
    }

    private void addComponentRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getSystems().stream().forEach(s -> {
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(s.getName());

            s.getContainers().stream().forEach(cont -> {
                Container container = softwareSystem.getContainerWithName(cont.getName());

                cont.getComponents().stream().forEach(comp -> {
                    Component component = container.getComponentWithName(comp.getName());

                    // Add component->system relationship
                    comp.getRelationships().stream()
                            .filter(r -> r.getType().equals(C4Type.system))
                            .forEach(r -> {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith());
                                String description = r.getDescription();
                                component.uses(systemDestination, description);
                            });

                    // Add component->container relationship
                    comp.getRelationships().stream()
                            .filter(r -> r.getType().equals(C4Type.container))
                            .forEach(r -> {
                                String systemName = r.getPath().getSystemName();
                                SoftwareSystem parentSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                                Container containerDestination = parentSystem.getContainerWithName(r.getWith());

                                String description = r.getDescription();
                                component.uses(containerDestination, description);
                            });

                    // Add component->component relationship
                    comp.getRelationships().stream()
                            .filter(r -> r.getType().equals(C4Type.component))
                            .forEach(r -> {
                                String systemName = r.getPath().getSystemName();
                                String containerName = r.getPath().getContainerName();
                                SoftwareSystem parentSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                                Container parentContainer = parentSystem.getContainerWithName(containerName);
                                Component componentDestination = parentContainer.getComponentWithName(r.getWith());

                                String description = r.getDescription();
                                component.uses(componentDestination, description);
                            });
                });
            });
        });
    }

    private void addContainerRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getSystems().stream().forEach(s -> {
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(s.getName());

            s.getContainers().stream().forEach(c -> {
                Container container = softwareSystem.getContainerWithName(c.getName());

                // Add container->system relationship
                c.getRelationships().stream()
                        .filter(r -> r.getType().equals(C4Type.system))
                        .forEach(r -> {
                            SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith());
                            String description = r.getDescription();
                            container.uses(systemDestination, description);
                        });

                // Add container->container relationship
                c.getRelationships().stream()
                        .filter(r -> r.getType().equals(C4Type.container))
                        .forEach(r -> {
                            String systemName = r.getPath().getSystemName();
                            SoftwareSystem parentSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                            Container containerDestination = parentSystem.getContainerWithName(r.getWith());

                            String description = r.getDescription();
                            container.uses(containerDestination, description);
                        });
            });
        });
    }

    private void addSystemRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getSystems().stream().forEach(s -> {
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(s.getName());


            // Add system->system relationships
            s.getRelationships().stream()
                    .filter(r -> r.getType().equals(C4Type.system))
                    .forEach(r -> {
                        SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith());
                        String description = r.getDescription();
                        softwareSystem.uses(systemDestination, description);
                    });

            // TODO: Add system->person `delivers` relationship (i.e. system emails user)
        });
    }

    private void addPeopleRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getPeople().stream().forEach(p -> {
            Person person = workspaceModel.getPersonWithName(p.getName());

            // Add persons->system relationships
            p.getRelationships().stream()
                    .filter(r -> r.getType().equals(C4Type.system))
                    .forEach(r -> {
                        SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(r.getWith());
                        String description = r.getDescription();
                        person.uses(softwareSystem, description);
                    });

            // Add persons->containers relationships
            p.getRelationships().stream()
                    .filter(r -> r.getType().equals(C4Type.container))
                    .forEach(r -> {
                        String systemName = r.getPath().getSystemName();
                        // Note: Without reference check system may be null
                        SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                        Container container = softwareSystem.getContainerWithName(r.getWith());

                        String description = r.getDescription();
                        person.uses(container, description);
                    });

            // Add persons->component relationships
            p.getRelationships().stream()
                    .filter(r -> r.getType().equals(C4Type.component))
                    .forEach(r -> {
                        String systemName = r.getPath().getSystemName();
                        String containerName = r.getPath().getContainerName();

                        // Note: Without reference check system may be null
                        SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
                        Container container = softwareSystem.getContainerWithName(containerName);
                        Component component = container.getComponentWithName(r.getWith());

                        String description = r.getDescription();
                        person.uses(component, description);
                    });
        });
    }
}
