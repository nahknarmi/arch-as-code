package net.trilogy.arch.domain.c4.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.DeploymentNode;
import com.structurizr.model.Location;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Container;
import net.trilogy.arch.domain.c4.C4DeploymentNode;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Path;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.domain.c4.C4Tag;
import net.trilogy.arch.domain.c4.HasTag;
import net.trilogy.arch.generator.FunctionalIdGenerator;

import java.util.List;

import static net.trilogy.arch.transformation.DeploymentNodeTransformer.addDeploymentNodeFromC4ToModel;
import static net.trilogy.arch.transformation.LocationTransformer.c4LocationToLocation;

public class ModelMediator {
    private static final ObjectMapper jackson = new ObjectMapper();
    private final Model model;
    private final FunctionalIdGenerator idGenerator;

    public ModelMediator(Model model, FunctionalIdGenerator idGenerator) {
        this.model = model;
        this.idGenerator = idGenerator;
    }

    public ModelMediator(Model model) {
        this.model = model;
        this.idGenerator = new FunctionalIdGenerator();
    }

    private static String str(List<String> lst) {
        try {
            return jackson.writeValueAsString(lst);
        } catch (final JsonProcessingException e) {
            final var x = new IllegalStateException("BUG: Impossible exception: " + e, e);
            x.setStackTrace(e.getStackTrace());
            throw x;
        }
    }

    public Person person(String id) {
        return (Person) model.getElement(id);
    }

    public Person person(C4Path path) {
        String id = path.getId();
        return (Person) model.getElement(id);
    }

    public SoftwareSystem softwareSystem(String id) {
        return (SoftwareSystem) model.getElement(id);
    }

    public SoftwareSystem softwareSystem(C4Path path) {
        String id = path.getId();
        return (SoftwareSystem) model.getElement(id);
    }

    public Container container(String id) {
        return (Container) model.getElement(id);
    }

    public Container container(C4Path path) {
        String id = path.getId();
        return (Container) model.getElement(id);
    }

    public Component component(String id) {
        return (Component) model.getElement(id);
    }

    public Component component(C4Path path) {
        String id = path.getId();
        return (Component) model.getElement(id);
    }

    public SoftwareSystem addSoftwareSystem(C4SoftwareSystem softwareSystem) {
        Location location = c4LocationToLocation(softwareSystem.getLocation());
        idGenerator.setNext(softwareSystem.getId());
        SoftwareSystem result = model.addSoftwareSystem(location, softwareSystem.getName(), softwareSystem.getDescription());
        result.addTags(getTags(softwareSystem));
        return result;
    }

    public Person addPerson(C4Person person) {
        // TODO [ENHANCEMENT]: Add aliases as property here (and also read them on import)
        Location location = c4LocationToLocation(person.getLocation());
        idGenerator.setNext(person.getId());
        Person result = model.addPerson(location, person.getName(), person.getDescription());
        result.addTags(getTags(person));
        return result;
    }

    public Container addContainer(C4SoftwareSystem system, C4Container container) {
        SoftwareSystem softwareSystem = new ModelMediator(model).softwareSystem(system.getId());
        idGenerator.setNext(container.getId());
        Container result = softwareSystem.addContainer(container.getName(), container.getDescription(), container.getTechnology());
        result.addTags(getTags(container));
        result.setUrl(container.getUrl());
        return result;
    }

    public Component addComponent(C4Container c4Container, C4Component component) {
        Container container = new ModelMediator(model).container(c4Container.getId());
        idGenerator.setNext(component.getId());
        Component result = container.addComponent(component.getName(), component.getDescription(), component.getTechnology());
        result.addTags(getTags(component));
        result.setUrl(component.getUrl());
        result.addProperty("Source Code Mappings", str(component.getSrcMappings()));
        return result;
    }

    public DeploymentNode addDeploymentNode(C4Model dataStructureModel, C4DeploymentNode c4DeploymentNode) {
        return addDeploymentNodeFromC4ToModel(c4DeploymentNode, dataStructureModel, model, idGenerator);
    }

    private String[] getTags(HasTag t) {
        return t.getTags().stream()
                .map(C4Tag::getTag)
                .toArray(String[]::new);
    }
}
