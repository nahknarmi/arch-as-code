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

    public static C4Person createPerson(String suffix) {
        return C4Person.builder()
                .id("p" + suffix)
                .name("person-" + suffix)
                .build();
    }

    public static C4Person createPersonWithRelationshipsTo(String suffix, Set<BaseEntity> entities) {
        final String personId = "p-" + suffix;
        final Set<C4Relationship> relationships = entities
                .stream()
                .map(e -> new C4Relationship(personId + "->" + e.getId(),
                                null,
                                C4Action.USES,
                                null,
                                e.getId(),
                                "desc-" + suffix,
                                null
                        )
                ).collect(Collectors.toSet());

        return C4Person.builder()
                .id(personId)
                .name("person-" + suffix)
                .relationships(relationships)
                .build();
    }

    public static C4SoftwareSystem createSystem(String suffix) {
        return C4SoftwareSystem.builder()
                .id("s" + suffix)
                .name("system-" + suffix)
                .build();
    }


    public static C4SoftwareSystem createSystemWithRelationshipsTo(String suffix, Set<BaseEntity> entities) {
        final String systemId = "s-" + suffix;
        final Set<C4Relationship> relationships = entities
                .stream()
                .map(e -> new C4Relationship(systemId + "->" + e.getId(),
                                null,
                                C4Action.USES,
                                null,
                                e.getId(),
                                "desc-" + suffix,
                                null
                        )
                ).collect(Collectors.toSet());

        return C4SoftwareSystem.builder()
                .id(systemId)
                .name("system-" + suffix)
                .relationships(relationships)
                .build();
    }
}
