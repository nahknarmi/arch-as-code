package net.nahknarmi.arch.domain.c4.view;

import com.structurizr.model.*;
import net.nahknarmi.arch.domain.c4.C4Path;

public class ModelMediator {


    public Person person(C4Path path, Model model) {
        String personName = path.getPersonName();
        return model.getPersonWithName(personName);
    }

    public SoftwareSystem system(C4Path path, Model model) {
        String systemName = path.getSystemName();
        return model.getSoftwareSystemWithName(systemName);
    }

    public Container container(C4Path path, Model model) {
        String systemName = path.getSystemName();
        String containerName = path.getContainerName()
                .orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
        SoftwareSystem softwareSystem = model.getSoftwareSystemWithName(systemName);
        return softwareSystem.getContainerWithName(containerName);
    }

    public Component component(C4Path path, Model model) {
        String systemName = path.getSystemName();
        String containerName = path.getContainerName()
                .orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
        String componentName = path.getComponentName()
                .orElseThrow(() -> new IllegalStateException("Workspace ID is missing!"));
        SoftwareSystem softwareSystem = model.getSoftwareSystemWithName(systemName);
        Container container = softwareSystem.getContainerWithName(containerName);
        return container.getComponentWithName(componentName);
    }

}
