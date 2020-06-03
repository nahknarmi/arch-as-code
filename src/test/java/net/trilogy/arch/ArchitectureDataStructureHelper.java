package net.trilogy.arch;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.view.C4ViewContainer;

import java.util.List;
import java.util.Set;

public class ArchitectureDataStructureHelper {
    public static ArchitectureDataStructure.ArchitectureDataStructureBuilder empty(String suffix) {
        return ArchitectureDataStructure
                .builder()
                .name("architecture-" + suffix)
                .businessUnit("business-Unit-" + suffix)
                .description("description-" + suffix)
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
}
