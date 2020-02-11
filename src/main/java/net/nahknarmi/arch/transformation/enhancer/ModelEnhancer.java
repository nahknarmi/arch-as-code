package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.generator.PathIdGenerator;
import net.nahknarmi.arch.transformation.LocationTransformer;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Model.NONE;

public class ModelEnhancer implements WorkspaceEnhancer {

    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        Model workspaceModel = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();

        workspaceModel.setIdGenerator(new PathIdGenerator(dataStructureModel));

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
                    Location location = LocationTransformer.c4LocationToLocation(p.getLocation());
                    Person person = model.addPerson(location, p.name(), p.getDescription());
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
        Location location = LocationTransformer.c4LocationToLocation(s.getLocation());
        SoftwareSystem softwareSystem = model.addSoftwareSystem(location, s.name(), s.getDescription());
        softwareSystem.addTags(getTags(s));
    }

    private void addContainers(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getContainers().forEach(c -> addContainer(workspaceModel, c));
    }

    private void addContainer(Model model, C4Container c) {
        String systemPath = c.getPath().systemPath().getPath();
        SoftwareSystem softwareSystem = (SoftwareSystem) model.getElement(systemPath);

        Container container = softwareSystem.addContainer(c.name(), c.getDescription(), c.getTechnology());
        container.addTags(getTags(c));
    }

    private void addComponents(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getComponents().forEach(c -> addComponent(workspaceModel, c));
    }

    private void addComponent(Model model, C4Component c) {
        Container container = (Container) model.getElement(c.getPath().containerPath().getPath());
        Component component = container.addComponent(c.name(), c.getDescription(), c.getTechnology());

        component.addTags(getTags(c));
    }

    private String[] getTags(HasTag t) {
        return t.getTags().stream().map(C4Tag::getTag).toArray(String[]::new);
    }

    private void addRelationships(Model workspaceModel, C4Model dataStructureModel) {
        addPeopleRelationships(workspaceModel, dataStructureModel);
        addSystemRelationships(workspaceModel, dataStructureModel);
        addContainerRelationships(workspaceModel, dataStructureModel);
        addComponentRelationships(workspaceModel, dataStructureModel);
    }

    private void addPeopleRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getPeople().forEach(p -> {
            Person person = (Person) workspaceModel.getElement(p.getPath().getPath());

            p.getRelationships()
                    .forEach(r -> {
                        String description = r.getDescription();
                        String technology = r.getTechnology();
                        C4Type typeDestination = r.getWith().type();

                        if (r.getAction() == C4Action.USES) {
                            String systemPath = r.getWith().systemPath().getPath();
                            SoftwareSystem systemDestination = (SoftwareSystem) workspaceModel.getElement(systemPath);

                            switch (typeDestination) {
                                case system: {
                                    person.uses(systemDestination, description, technology);
                                    break;
                                }
                                case container: {
                                    Container container = (Container) workspaceModel.getElement(r.getWith().containerPath().getPath());

                                    person.uses(container, description, technology);
                                    break;
                                }
                                case component: {
                                    Component component = (Component) workspaceModel.getElement(r.getWith().componentPath().getPath());

                                    person.uses(component, description, technology);
                                    break;
                                }
                                default:
                                    throw new IllegalStateException("Unsupported type " + typeDestination);
                            }
                        }
                        if (r.getAction() == C4Action.INTERACTS_WITH) {
                            if (typeDestination != C4Type.person) {
                                throw new IllegalStateException("Action INTERACTS_WITH supported only with type person, not: " + typeDestination);
                            } else {
                                Person personDestination = (Person) workspaceModel.getElement(r.getWith().getPath());

                                person.interactsWith(personDestination, description, technology);
                            }
                        }
                    });
        });
    }

    private void addSystemRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getSystems().forEach(s -> {
            SoftwareSystem softwareSystem = (SoftwareSystem) workspaceModel.getElement(s.getPath().systemPath().getPath());

            s.getRelationships()
                    .forEach(r -> {
                        String description = r.getDescription();
                        String technology = r.getTechnology();
                        C4Type typeDestination = r.getWith().type();

                        if (r.getAction() == C4Action.USES) {
                            SoftwareSystem systemDestination = (SoftwareSystem) workspaceModel.getElement(r.getWith().systemPath().getPath());

                            switch (typeDestination) {
                                case system: {
                                    softwareSystem.uses(systemDestination, description, technology);
                                    break;
                                }
                                case container: {
                                    Container containerDestination = (Container) workspaceModel.getElement(r.getWith().containerPath().getPath());
                                    softwareSystem.uses(containerDestination, description, technology);
                                    break;
                                }
                                case component: {
                                    Component component = (Component) workspaceModel.getElement(r.getWith().componentPath().getPath());
                                    softwareSystem.uses(component, description, technology);
                                    break;
                                }
                                default:
                                    throw new IllegalStateException("Unsupported type " + typeDestination);
                            }
                        }
                        if (r.getAction() == C4Action.DELIVERS) {
                            if (typeDestination != C4Type.person) {
                                throw new IllegalStateException("Action DELIVERS supported only with type person, not: " + typeDestination);
                            } else {
                                String personId = r.getWith().getPath();
                                Person personDestination = (Person) workspaceModel.getElement(personId);
                                softwareSystem.delivers(personDestination, description, technology);
                            }
                        }
                    });
        });
    }

    private void addContainerRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getContainers().forEach(c -> {
            Container container = (Container) workspaceModel.getElement(c.getPath().containerPath().getPath());

            c.getRelationships()
                    .forEach(r -> {
                        String description = r.getDescription();
                        String technology = r.getTechnology();
                        C4Type typeDestination = r.getWith().type();

                        if (r.getAction() == C4Action.USES) {
                            SoftwareSystem systemDestination = (SoftwareSystem) workspaceModel.getElement(r.getWith().systemPath().getPath());

                            switch (typeDestination) {
                                case system: {
                                    container.uses(systemDestination, description, technology);
                                    break;
                                }
                                case container: {
                                    Container containerDestination = (Container) workspaceModel.getElement(r.getWith().containerPath().getPath());

                                    container.uses(containerDestination, description, technology);
                                    break;
                                }
                                case component: {
                                    Component componentDestination = (Component) workspaceModel.getElement(r.getWith().componentPath().getPath());

                                    if (componentDestination == null) {
                                        System.err.println("Hanging reference - " + r.getWith());
                                    } else {
                                        container.uses(componentDestination, description, technology);
                                    }

                                    break;
                                }
                                default:
                                    throw new IllegalStateException("Unsupported type " + typeDestination);
                            }
                        }
                        if (r.getAction() == C4Action.DELIVERS) {
                            if (typeDestination != C4Type.person) {
                                throw new IllegalStateException("Action DELIVERS supported only with type person, not: " + typeDestination);
                            } else {
                                String personId = r.getWith().getPath();
                                Person personDestination = (Person) workspaceModel.getElement(personId);
                                container.delivers(personDestination, description, technology);
                            }
                        }
                    });
        });
    }

    private void addComponentRelationships(Model workspaceModel, C4Model dataStructureModel) {
        dataStructureModel.getComponents().forEach(comp -> {
            Component component = (Component) workspaceModel.getElement(comp.getPath().componentPath().getPath());

            comp.getRelationships()
                    .forEach(r -> {
                        String description = r.getDescription();
                        String technology = r.getTechnology();
                        C4Type typeDestination = r.getWith().type();

                        if (r.getAction() == C4Action.USES) {
                            SoftwareSystem systemDestination = (SoftwareSystem) workspaceModel.getElement(r.getWith().systemPath().getPath());

                            switch (typeDestination) {
                                case system: {
                                    component.uses(systemDestination, description, technology);
                                    break;
                                }
                                case container: {
                                    Container containerDestination = (Container) workspaceModel.getElement(r.getWith().containerPath().getPath());
                                    component.uses(containerDestination, description, technology);
                                    break;
                                }
                                case component: {
                                    Component componentDestination = (Component) workspaceModel.getElement(r.getWith().componentPath().getPath());

                                    if (componentDestination == null) {
                                        System.err.println("Hanging reference " + r.getWith());
                                    } else {
                                        component.uses(componentDestination, description, technology);
                                    }

                                    break;
                                }
                                default:
                                    throw new IllegalStateException("Unsupported type " + typeDestination);
                            }
                        }
                        if (r.getAction() == C4Action.DELIVERS) {
                            if (typeDestination != C4Type.person) {
                                throw new IllegalStateException("Action DELIVERS supported only with type person, not: " + typeDestination);
                            } else {
                                String personId = r.getWith().getPath();
                                Person personDestination = (Person) workspaceModel.getElement(personId);
                                component.delivers(personDestination, description, technology);
                            }
                        }
                    });
        });
    }
}
