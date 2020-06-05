package net.trilogy.arch;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.*;
import net.trilogy.arch.domain.c4.view.C4ViewContainer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArchitectureDataStructureHelper {
    public static ArchitectureDataStructure.ArchitectureDataStructureBuilder emptyArch() {
        return ArchitectureDataStructure
                .builder()
                .name("architecture")
                .businessUnit("business-Unit")
                .description("description")
                .decisions(List.of())
                .model(emptyModel())
                .views(emptyViews());
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

    public static C4ViewContainer emptyViews() {
        return new C4ViewContainer(
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public static C4Person createPerson(String id) {
        return C4Person.builder()
                .id(id)
                .name("person-" + id)
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


    public static C4SoftwareSystem createSystemWithRelationshipsTo(String id, Set<BaseEntity> entities) {
        final String systemId = id;
        final Set<C4Relationship> relationships = entities
                .stream()
                .map(e -> new C4Relationship(systemId + "->" + e.getId(),
                                null,
                                C4Action.USES,
                                null,
                                e.getId(),
                                "desc-" + id,
                                null
                        )
                ).collect(Collectors.toSet());

        return C4SoftwareSystem.builder()
                .id(systemId)
                .name("system-" + id)
                .relationships(relationships)
                .build();
    }
}
