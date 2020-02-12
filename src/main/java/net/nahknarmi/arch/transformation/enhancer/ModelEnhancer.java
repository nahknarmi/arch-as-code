package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import io.vavr.Tuple2;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;
import net.nahknarmi.arch.generator.PathIdGenerator;

import java.util.Set;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Action.DELIVERS;
import static net.nahknarmi.arch.domain.c4.C4Action.INTERACTS_WITH;
import static net.nahknarmi.arch.domain.c4.C4Model.NONE;

public class ModelEnhancer implements WorkspaceEnhancer {

    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        Model workspaceModel = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();
        workspaceModel.setIdGenerator(new PathIdGenerator(dataStructureModel));

        ModelMediator modelMediator = new ModelMediator(workspaceModel);

        addPeople(dataStructureModel, modelMediator);
        addSystems(dataStructureModel, modelMediator);
        addContainers(dataStructureModel, modelMediator);
        addComponents(dataStructureModel, modelMediator);
        addRelationships(dataStructureModel, modelMediator);
    }

    private void addPeople(C4Model dataStructureModel, ModelMediator modelMediator) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getPeople()
                .forEach(modelMediator::addPerson);
    }

    private void addSystems(C4Model dataStructureModel, ModelMediator modelMediator) {
        ofNullable(dataStructureModel)
                .orElse(NONE)
                .getSystems()
                .forEach(modelMediator::addSoftwareSystem);
    }

    private void addContainers(C4Model dataStructureModel, ModelMediator modelMediator) {
        dataStructureModel.getContainers().forEach(modelMediator::addContainer);
    }

    private void addComponents(C4Model dataStructureModel, ModelMediator modelMediator) {
        dataStructureModel.getComponents().forEach(modelMediator::addComponent);
    }

    private void addRelationships(C4Model dataStructureModel, ModelMediator modelMediator) {
        addPeopleRelationships(modelMediator, dataStructureModel.getPeople(), x -> new Tuple2<>(x, modelMediator.person(x.getPath())));
        addNonPersonRelationships(modelMediator, dataStructureModel.getSystems(), x -> new Tuple2<>(x, modelMediator.softwareSystem(x.getPath())));
        addNonPersonRelationships(modelMediator, dataStructureModel.getContainers(), x -> new Tuple2<>(x, modelMediator.container(x.getPath())));
        addNonPersonRelationships(modelMediator, dataStructureModel.getComponents(), x -> new Tuple2<>(x, modelMediator.component(x.getPath())));
    }

    private void addPeopleRelationships(ModelMediator modelMediator, Set<? extends BaseEntity> entities, Function<BaseEntity, ? extends Tuple2<? extends BaseEntity, StaticStructureElement>> tuple2Function) {
        entities.stream().map(tuple2Function)
                .forEach(tuple2 ->
                        tuple2._1()
                                .getRelationships()
                                .forEach(r -> {
                                    addUsesRelationship(modelMediator, tuple2._2(), r);
                                    addInteractsWith(modelMediator, (Person) tuple2._2(), r);
                                })
                );
    }

    private void addNonPersonRelationships(ModelMediator modelMediator, Set<? extends BaseEntity> entities, Function<BaseEntity, ? extends Tuple2<? extends BaseEntity, StaticStructureElement>> tuple2Function) {
        entities.stream()
                .map(tuple2Function)
                .forEach(tuple2 -> addUsesAndDeliversRelations(modelMediator, tuple2._1(), tuple2._2()));
    }

    private void addUsesAndDeliversRelations(ModelMediator modelMediator, HasRelation c, StaticStructureElement container) {
        c.getRelationships()
                .forEach(r -> {
                    addUsesRelationship(modelMediator, container, r);
                    addDelivers(modelMediator, container, r);
                });
    }

    private void addUsesRelationship(ModelMediator modelMediator, StaticStructureElement element, C4Relationship r) {
        if (C4Action.USES.equals(r.getAction())) {
            SoftwareSystem systemDestination = modelMediator.softwareSystem(r.getWith());

            switch (r.getWith().type()) {
                case system: {
                    element.uses(systemDestination, r.getDescription(), r.getTechnology());
                    break;
                }
                case container: {
                    Container containerDestination = modelMediator.container(r.getWith());
                    element.uses(containerDestination, r.getDescription(), r.getTechnology());
                    break;
                }
                case component: {
                    Component component = modelMediator.component(r.getWith());
                    element.uses(component, r.getDescription(), r.getTechnology());
                    break;
                }
                default:
                    throw new IllegalStateException("Unsupported type " + r.getWith().type());
            }
        }
    }

    private void addDelivers(ModelMediator modelMediator, StaticStructureElement element, C4Relationship r) {
        if (DELIVERS.equals(r.getAction())) {
            if (!C4Type.person.equals(r.getWith().type())) {
                throw new IllegalStateException("Action DELIVERS supported only with type person, not: " + r.getWith().type());
            } else {
                Person person = modelMediator.person(r.getWith());
                element.delivers(person, r.getDescription(), r.getTechnology());
            }
        }
    }

    private void addInteractsWith(ModelMediator modelMediator, Person person, C4Relationship r) {
        if (INTERACTS_WITH.equals(r.getAction())) {
            if (!C4Type.person.equals(r.getWith().type())) {
                throw new IllegalStateException("Action INTERACTS_WITH supported only with type person, not: " + r.getWith().type());
            } else {
                Person personDestination = modelMediator.person(r.getWith());
                person.interactsWith(personDestination, r.getDescription(), r.getTechnology());
            }
        }
    }
}
