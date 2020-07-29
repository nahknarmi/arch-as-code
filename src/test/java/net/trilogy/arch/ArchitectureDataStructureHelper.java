package net.trilogy.arch;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

public class ArchitectureDataStructureHelper {
    public static ArchitectureDataStructure.ArchitectureDataStructureBuilder emptyArch() {
        return ArchitectureDataStructure
                .builder()
                .name("architecture")
                .businessUnit("business-Unit")
                .description("description")
                .decisions(List.of())
                .model(emptyModel());
    }

    public static C4Model emptyModel() {
        return new C4Model(
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of()
        );
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
            .name("component-"+id)
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
        C4SoftwareSystem softwareSystem = softwareSystem();
        softwareSystem.setId(systemId);
        softwareSystem.setPath(C4Path.path("c4://ABC"));
        C4Container container = createContainer(containerId, systemId);
        container.setPath(C4Path.path("c4://ABC/C1"));
        model.addSoftwareSystem(softwareSystem);
        model.addContainer(container);
        return model;
    }

    public static C4SoftwareSystem createSystemWithRelationshipsTo(String id, Set<Entity> entities) {
        final String systemId = id;
        final Set<C4Relationship> relationships = entities
                .stream()
                .map(e -> new C4Relationship(systemId + "->" + e.getId(),
                                null,
                                C4Action.USES,
                                null,
                                e.getId(),
                                "desc-" + id,
                                "HTTPS"
                        )
                ).collect(Collectors.toSet());

        return C4SoftwareSystem.builder()
                .id(systemId)
                .name("system-" + id)
                .relationships(relationships)
                .build();
    }
}
