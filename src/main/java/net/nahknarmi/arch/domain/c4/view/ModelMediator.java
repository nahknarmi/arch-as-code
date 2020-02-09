package net.nahknarmi.arch.domain.c4.view;

import com.structurizr.model.*;
import net.nahknarmi.arch.domain.c4.C4Path;

public class ModelMediator {
    private final Model model;

    public ModelMediator(Model model) {
        this.model = model;
    }

    public Person person(C4Path path) {
        String personName = path.personName();
        return model.getPersonWithName(personName);
    }

    public SoftwareSystem softwareSystem(C4Path path) {
        String systemName = path.systemName();
        return model.getSoftwareSystemWithName(systemName);
    }

    public Container container(C4Path path) {
        String containerName = path.containerName()
                .orElseThrow(() -> new IllegalStateException(String.format("No container was specified in path - %s. " +
                        "Is your container path correct?", path)));
        SoftwareSystem softwareSystem = softwareSystem(path);
        return softwareSystem.getContainerWithName(containerName);
    }

    public Component component(C4Path path) {
        String componentName = path.componentName()
                .orElseThrow(() -> new IllegalStateException(String.format("No component was specified in path - %s. " +
                        "Is your component path correct?", path)));
        Container container = container(path);
        return container.getComponentWithName(componentName);
    }

}
