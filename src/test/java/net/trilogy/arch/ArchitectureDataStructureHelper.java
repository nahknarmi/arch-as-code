package net.trilogy.arch;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.ArchitectureDataStructure.ArchitectureDataStructureBuilder;
import net.trilogy.arch.domain.c4.C4Action;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Container;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Path;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4Relationship;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.domain.c4.Entity;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class ArchitectureDataStructureHelper {
    public static ArchitectureDataStructureBuilder emptyArch() {
        return ArchitectureDataStructure.builder()
                .name("architecture")
                .businessUnit("business-Unit")
                .description("description")
                .decisions(List.of())
                .model(C4Model.empty());
    }

    public static C4Person createPerson(String id) {
        return C4Person.builder()
                .id(id)
                .name("person-" + id)
                .build();
    }

    public static C4Relationship createRelationship(String id, String withId) {
        return C4Relationship.builder()
                .id(id)
                .alias("a" + id)
                .withId(withId)
                .action(C4Action.INTERACTS_WITH)
                .technology("t" + id)
                .description("d" + id)
                .build();
    }

    public static C4Person createPersonWithRelationshipsTo(String id, Set<C4Relationship> relationships) {
        return C4Person.builder()
                .id(id)
                .name("person-" + id)
                .relationships(relationships)
                .build();
    }

    public static C4SoftwareSystem createSystem(String id) {
        return C4SoftwareSystem.builder()
                .id(id)
                .name("system-" + id)
                .build();
    }

    public static C4Container createContainer(String id, String systemId) {
        return C4Container.builder()
                .id(id)
                .name("container-" + id)
                .systemId(systemId)
                .build();
    }

    public static C4Component createComponent(String id, String containerId) {
        return C4Component.builder()
                .id(id)
                .name("component-" + id)
                .containerId(containerId)
                .build();
    }

    public static C4SoftwareSystem softwareSystem() {
        return C4SoftwareSystem.builder()
                .id("1")
                .alias("c4://OBP")
                .name("OBP")
                .description("core banking")
                .tags(emptySet())
                .relationships(emptyList())
                .build();
    }

    public static C4Model addSystemWithContainer(C4Model model, String systemId, String containerId) {
        final var softwareSystem = softwareSystem();
        softwareSystem.setId(systemId);
        softwareSystem.setPath(C4Path.path("c4://ABC"));
        final var container = createContainer(containerId, systemId);
        container.setPath(C4Path.path("c4://ABC/C1"));
        model.addSoftwareSystem(softwareSystem);
        model.addContainer(container);
        return model;
    }

    public static C4SoftwareSystem createSystemWithRelationshipsTo(String systemId, Set<Entity> entities) {
        final var relationships = entities.stream()
                .map(e -> new C4Relationship(systemId + "->" + e.getId(),
                        null,
                        C4Action.USES,
                        null,
                        e.getId(),
                        "desc-" + systemId,
                        "HTTPS"
                ))
                .collect(toSet());

        return C4SoftwareSystem.builder()
                .id(systemId)
                .name("system-" + systemId)
                .relationships(relationships)
                .build();
    }
}
