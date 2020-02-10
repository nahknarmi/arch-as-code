package net.nahknarmi.arch.domain.c4.view;

import com.structurizr.model.*;
import net.nahknarmi.arch.domain.c4.C4Path;

public class ModelMediator {
    private final Model model;

    public ModelMediator(Model model) {
        this.model = model;
    }

    public Person person(C4Path path) {
        String id = path.getPath();
        Person element = (Person) model.getElement(id);
        return element;
    }

    public SoftwareSystem softwareSystem(C4Path path) {
        String id = path.getPath();
        SoftwareSystem element = (SoftwareSystem) model.getElement(id);
        return element;
    }

    public Container container(C4Path path) {
        String id = path.getPath();
        Container element = (Container) model.getElement(id);
        return element;
    }

    public Component component(C4Path path) {
        String id = path.getPath();
        Component element = (Component) model.getElement(id);
        return element;
    }

}
