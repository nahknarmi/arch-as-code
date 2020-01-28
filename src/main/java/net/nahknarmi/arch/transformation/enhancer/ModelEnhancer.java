package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Model.NONE;

public class ModelEnhancer implements WorkspaceEnhancer {

    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        Model workspaceModel = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();

        addPeople(workspaceModel, dataStructureModel);
        addSystems(workspaceModel, dataStructureModel);
        addContainers(workspaceModel, dataStructureModel);
        addComponents(workspaceModel, dataStructureModel);
        addRelationships(workspaceModel, dataStructureModel);
    }

    private void addPeople(Model model, C4Model dataStructureModel) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getPeople()
                .forEach(p -> {
                    Person person = model.addPerson(p.getName(), p.getDescription());
                    person.addTags(getTags(p));
                });
    }

    private void addSystems(Model model, C4Model dataStructureModel) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getSystems()
                .forEach(s -> addSystem(model, s));
    }

    private void addSystem(Model model, C4SoftwareSystem s) {
        SoftwareSystem softwareSystem = model.addSoftwareSystem(s.getName(), s.getDescription());
        softwareSystem.addTags(getTags(s));
    }

    private void addContainers(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getContainers().stream().forEach(c -> addContainer(workspaceModel, c));
    }

    private void addContainer(Model model, C4Container c) {
        String systemName = c.getPath().getSystemName();
        String containerName = c.getName();
        SoftwareSystem softwareSystem = model.getSoftwareSystemWithName(systemName);

        Container container = softwareSystem.addContainer(containerName, c.getDescription(), c.getTechnology());
        container.addTags(getTags(c));
    }

    private void addComponents(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getComponents().stream().forEach(c -> addComponent(workspaceModel, c));
    }

    private void addComponent(Model model, C4Component c) {
        String systemName = c.getPath().getSystemName();
        String containerName = c.getPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace Id not found!"));
        SoftwareSystem softwareSystem = model.getSoftwareSystemWithName(systemName);
        Container container = softwareSystem.getContainerWithName(containerName);

        Component component = container.addComponent(c.getName(), c.getDescription(), c.getTechnology());
        component.addTags(getTags(c));
    }

    private String[] getTags(Tagable t) {
        List<String> stringList = t.getTags().stream().map(tag -> tag.getTag()).collect(Collectors.toList());
        return stringList.toArray(new String[stringList.size()]);
    }

    private void addRelationships(Model workspaceModel, C4Model dataStructureModel) {
        addPeopleRelationships(workspaceModel, dataStructureModel);
        addSystemRelationships(workspaceModel, dataStructureModel);
        addContainerRelationships(workspaceModel, dataStructureModel);
        addComponentRelationships(workspaceModel, dataStructureModel);
    }

    private void addPeopleRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getPeople().stream().forEach(p -> {
            Person person = workspaceModel.getPersonWithName(p.getName());

            p.getRelationships().stream()
                    .forEach(r -> {
                        String description = r.getDescription();

                        switch (r.getWith().getType()) {
                            case system: {
                                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                person.uses(softwareSystem, description);
                                break;
                            }
                            case container: {
                                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                Container container = softwareSystem.getContainerWithName(r.getWith().getName());
                                person.uses(container, description);
                                break;
                            }
                            case component: {
                                SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                Container container = softwareSystem.getContainerWithName(r.getWith().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace Id not found!")));
                                Component component = container.getComponentWithName(r.getWith().getName());
                                person.uses(component, description);
                                break;
                            }
                            default:
                                throw new IllegalStateException("Unsupported type " + r.getWith().getType());
                        }
                    });
        });
    }

    private void addSystemRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getSystems().stream().forEach(s -> {
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(s.getName());


            s.getRelationships().stream()
                    .forEach(r -> {
                        String description = r.getDescription();
                        switch (r.getWith().getType()) {
                            case system: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getName());
                                softwareSystem.uses(systemDestination, description);
                                break;
                                // TODO: Add system->person `delivers` relationship (i.e. system emails user)
                            }
                            default:
                                throw new IllegalStateException("Unsupported type " + r.getWith().getType());
                        }
                    });
        });
    }

    private void addContainerRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getContainers().stream().forEach(c -> {
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(c.getPath().getSystemName());
            Container container = softwareSystem.getContainerWithName(c.getName());

            c.getRelationships().stream()
                    .forEach(r -> {
                        String description = r.getDescription();
                        switch (r.getWith().getType()) {
                            case system: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                container.uses(systemDestination, description);
                                break;
                            }
                            case container: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                String containerName = r.getWith().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                                Container containerDestination = systemDestination.getContainerWithName(containerName);
                                container.uses(containerDestination, description);
                                break;
                            }
                            case component: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                String containerName = r.getWith().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                                String componentName = r.getWith().getComponentName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
                                Container containerDestination = systemDestination.getContainerWithName(containerName);
                                Component componentDestination = containerDestination.getComponentWithName(componentName);
                                container.uses(componentDestination, description);
                                break;
                            }
                            default:
                                throw new IllegalStateException("Unsupported type " + r.getWith().getType());
                        }
                    });
        });
    }

    private void addComponentRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getComponents().stream().forEach(comp -> {
            SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(comp.getPath().getSystemName());
            String containerName = comp.getPath().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace Id not found!"));
            Container container = softwareSystem.getContainerWithName(containerName);
            Component component = container.getComponentWithName(comp.getName());
            comp.getRelationships().stream()
                    .forEach(r -> {
                        String description = ofNullable(r.getDescription()).orElse("");
                        switch (r.getWith().getType()) {
                            case system: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                component.uses(systemDestination, description);
                                break;
                            }
                            case container: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                Container containerDestination = systemDestination.getContainerWithName(r.getWith().getName());
                                component.uses(containerDestination, description);
                                break;
                            }
                            case component: {
                                SoftwareSystem systemDestination = workspaceModel.getSoftwareSystemWithName(r.getWith().getSystemName());
                                Container containerDestination = systemDestination.getContainerWithName(r.getWith().getContainerName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!")));
                                Component componentDestination = containerDestination.getComponentWithName(r.getWith().getComponentName().orElseThrow(() -> new IllegalStateException("Workspace ID is missing!")));
                                component.uses(componentDestination, description);
                                break;
                            }
                            default:
                                throw new IllegalStateException("Unsupported type " + r.getWith().getType());
                        }
                    });
        });
    }
}
