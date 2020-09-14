package net.trilogy.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.model.StaticStructureElement;
import io.vavr.Tuple2;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Action;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Container;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Relationship;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.domain.c4.C4Type;
import net.trilogy.arch.domain.c4.Entity;
import net.trilogy.arch.domain.c4.HasRelation;
import net.trilogy.arch.domain.c4.view.ModelMediator;
import net.trilogy.arch.generator.FunctionalIdGenerator;

import java.util.Set;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static net.trilogy.arch.domain.c4.C4Action.DELIVERS;
import static net.trilogy.arch.domain.c4.C4Action.INTERACTS_WITH;
import static net.trilogy.arch.domain.c4.C4Model.EMPTY;
import static net.trilogy.arch.domain.c4.C4Type.PERSON;

public class ModelEnhancer implements WorkspaceEnhancer {
    private final FunctionalIdGenerator idGenerator = new FunctionalIdGenerator();

    private static void addPeople(C4Model dataStructureModel, ModelMediator modelMediator) {
        ofNullable(dataStructureModel)
                .orElse(EMPTY)
                .getPeople()
                .forEach(modelMediator::addPerson);
    }

    private static void addSystems(C4Model dataStructureModel, ModelMediator modelMediator) {
        ofNullable(dataStructureModel)
                .orElse(EMPTY)
                .getSystems()
                .forEach(modelMediator::addSoftwareSystem);
    }

    private static void addContainers(C4Model dataStructureModel, ModelMediator modelMediator) {
        dataStructureModel.getContainers().forEach(cont -> {
            C4SoftwareSystem sys = getSoftwareSystem(dataStructureModel, cont);
            modelMediator.addContainer(sys, cont);
        });
    }

    private static void addComponents(C4Model dataStructureModel, ModelMediator modelMediator) {
        dataStructureModel.getComponents().forEach(comp -> {
            C4Container cont = getContainer(dataStructureModel, comp);
            modelMediator.addComponent(cont, comp);
        });
    }

    private static void addDeploymentNodes(C4Model dataStructureModel, ModelMediator modelMediator) {
        dataStructureModel.getDeploymentNodes().forEach(dNode -> modelMediator.addDeploymentNode(dataStructureModel, dNode));
    }

    private static C4Container getContainer(C4Model dataStructureModel, C4Component comp) {
        Entity result;
        if (comp.getContainerId() != null) {
            String id = comp.getContainerId();
            result = dataStructureModel.findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id));
        } else if (comp.getContainerAlias() != null) {
            result = dataStructureModel.findEntityByAlias(comp.getContainerAlias());
        } else {
            throw new IllegalStateException("Container containerId and containerAlias are both missing: " + comp);
        }

        if (result instanceof C4Container) {
            return (C4Container) result;
        } else {
            throw new IllegalStateException("Container containerId/systemAlias is not referencing system: " + comp);
        }
    }

    private static C4SoftwareSystem getSoftwareSystem(C4Model dataStructureModel, C4Container cont) {
        Entity result;
        if (cont.getSystemId() != null) {
            String id = cont.getSystemId();
            result = dataStructureModel.findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id));
        } else if (cont.getSystemAlias() != null) {
            result = dataStructureModel.findEntityByAlias(cont.getSystemAlias());
        } else {
            throw new IllegalStateException("Container systemId and systemAlias are both missing: " + cont);
        }

        if (result instanceof C4SoftwareSystem) {
            return (C4SoftwareSystem) result;
        } else {
            throw new IllegalStateException("Container systemId/systemAlias is not referencing system: " + cont);
        }
    }

    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        Model workspaceModel = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();

        workspaceModel.setIdGenerator(idGenerator);
        ModelMediator modelMediator = new ModelMediator(workspaceModel, idGenerator);

        addPeople(dataStructureModel, modelMediator);
        addSystems(dataStructureModel, modelMediator);
        addContainers(dataStructureModel, modelMediator);
        addComponents(dataStructureModel, modelMediator);
        addRelationships(dataStructureModel, modelMediator);
        addDeploymentNodes(dataStructureModel, modelMediator);
    }

    private void addRelationships(C4Model dataStructureModel, ModelMediator modelMediator) {
        addPeopleRelationships(modelMediator, dataStructureModel, dataStructureModel.getPeople(), person -> new Tuple2<>(person, modelMediator.person(person.getId())));
        addNonPersonRelationships(modelMediator, dataStructureModel, dataStructureModel.getSystems(), sys -> new Tuple2<>(sys, modelMediator.softwareSystem(sys.getId())));
        addNonPersonRelationships(modelMediator, dataStructureModel, dataStructureModel.getContainers(), cont -> new Tuple2<>(cont, modelMediator.container(cont.getId())));
        addNonPersonRelationships(modelMediator, dataStructureModel, dataStructureModel.getComponents(), comp -> new Tuple2<>(comp, modelMediator.component(comp.getId())));
    }

    private void addPeopleRelationships(ModelMediator modelMediator, C4Model dataStructureModel, Set<? extends Entity> entities, Function<Entity, ? extends Tuple2<? extends Entity, StaticStructureElement>> tuple2Function) {
        entities.stream().map(tuple2Function)
                .forEach(tuple2 ->
                        tuple2._1()
                                .getRelationships()
                                .forEach(r -> {
                                    addUsesRelationship(modelMediator, dataStructureModel, tuple2._2(), r);
                                    addInteractsWith(modelMediator, dataStructureModel, (Person) tuple2._2(), r);
                                })
                );
    }

    private void addNonPersonRelationships(ModelMediator modelMediator, C4Model dataStructureModel, Set<? extends Entity> entities, Function<Entity, ? extends Tuple2<? extends Entity, StaticStructureElement>> tuple2Function) {
        entities.stream()
                .map(tuple2Function)
                .forEach(tuple2 -> addUsesAndDeliversRelations(modelMediator, dataStructureModel, tuple2._1(), tuple2._2()));
    }

    private void addUsesAndDeliversRelations(ModelMediator modelMediator, C4Model dataStructureModel, HasRelation c, StaticStructureElement container) {
        c.getRelationships()
                .forEach(r -> {
                    addUsesRelationship(modelMediator, dataStructureModel, container, r);
                    addDelivers(modelMediator, dataStructureModel, container, r);
                });
    }

    private void addUsesRelationship(ModelMediator modelMediator, C4Model dataStructureModel, StaticStructureElement element, C4Relationship r) {
        if (r.getAction() == C4Action.USES) {
            Entity destination = dataStructureModel.findEntityByRelationshipWith(r);
            C4Type type = destination.getType();
            idGenerator.setNext(r.getId());

            switch (type) {
                case SYSTEM: {
                    SoftwareSystem systemDestination = modelMediator.softwareSystem(destination.getId());
                    element.uses(systemDestination, r.getDescription(), r.getTechnology());
                    break;
                }
                case CONTAINER: {
                    Container containerDestination = modelMediator.container(destination.getId());
                    element.uses(containerDestination, r.getDescription(), r.getTechnology());
                    break;
                }
                case COMPONENT: {
                    Component component = modelMediator.component(destination.getId());
                    element.uses(component, r.getDescription(), r.getTechnology());
                    break;
                }
                default:
                    throw new IllegalStateException("Unsupported type " + type);
            }
        }
    }

    private void addDelivers(ModelMediator modelMediator, C4Model dataStructureModel, StaticStructureElement element, C4Relationship r) {
        if (r.getAction().equals(DELIVERS)) {
            Entity destination = dataStructureModel.findEntityByRelationshipWith(r);
            String destinationId = destination.getId();
            C4Type type = destination.getType();

            if (type.equals(PERSON)) {
                Person person = modelMediator.person(destinationId);
                idGenerator.setNext(r.getId());
                element.delivers(person, r.getDescription(), r.getTechnology());
            } else {
                throw new IllegalStateException("Action DELIVERS supported only with type person, not: " + type);
            }
        }
    }

    private void addInteractsWith(ModelMediator modelMediator, C4Model dataStructureModel, Person person, C4Relationship r) {
        if (r.getAction().equals(INTERACTS_WITH)) {
            Entity destination = dataStructureModel.findEntityByRelationshipWith(r);
            String destinationId = destination.getId();
            C4Type type = destination.getType();

            if (type.equals(PERSON)) {
                Person personDestination = modelMediator.person(destinationId);
                idGenerator.setNext(r.getId());
                person.interactsWith(personDestination, r.getDescription(), r.getTechnology());
            } else {
                throw new IllegalStateException("Action INTERACTS_WITH supported only with type person, not: " + type);
            }
        }
    }
}
