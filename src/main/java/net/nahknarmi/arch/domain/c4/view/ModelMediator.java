package net.nahknarmi.arch.domain.c4.view;

import com.structurizr.model.*;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.transformation.LocationTransformer;

public class ModelMediator {
    private final Model model;

    public ModelMediator(Model model) {
        this.model = model;
    }

    public Person person(C4Path path) {
        String id = path.getPath();
        return (Person) model.getElement(id);
    }

    public SoftwareSystem softwareSystem(C4Path path) {
        String id = path.systemPath().getPath();
        return (SoftwareSystem) model.getElement(id);
    }

    public Container container(C4Path path) {
        String id = path.containerPath().getPath();
        return (Container) model.getElement(id);
    }

    public Component component(C4Path path) {
        String id = path.componentPath().getPath();
        return (Component) model.getElement(id);
    }


    public SoftwareSystem addSoftwareSystem(C4SoftwareSystem softwareSystem) {
        Location location = LocationTransformer.c4LocationToLocation(softwareSystem.getLocation());
        SoftwareSystem result = this.model.addSoftwareSystem(location, softwareSystem.name(), softwareSystem.getDescription());
        result.addTags(getTags(softwareSystem));
        return result;
    }

    public Person addPerson(C4Person person) {
        Location location = LocationTransformer.c4LocationToLocation(person.getLocation());
        Person result = model.addPerson(location, person.name(), person.getDescription());
        result.addTags(getTags(person));
        return result;
    }

    public Container addContainer(C4Container container) {
        SoftwareSystem softwareSystem = new ModelMediator(model).softwareSystem(container.getPath().systemPath());
        Container result = softwareSystem.addContainer(container.name(), container.getDescription(), container.getTechnology());
        result.addTags(getTags(container));
        return result;
    }

    public Component addComponent(C4Component component) {
        Container container = new ModelMediator(model).container(component.getPath().containerPath());
        Component result = container.addComponent(component.name(), component.getDescription(), component.getTechnology());
        result.addTags(getTags(component));
        return result;
    }

    private String[] getTags(HasTag t) {
        return t.getTags().stream().map(C4Tag::getTag).toArray(String[]::new);
    }
}
