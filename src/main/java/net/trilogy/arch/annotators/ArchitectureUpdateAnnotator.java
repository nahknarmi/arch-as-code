package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.c4.C4Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ArchitectureUpdateAnnotator {
    private static List<TddContainerByComponent> updatePathBasedOnId(Set<C4Component> c4Components, List<TddContainerByComponent> withUpdatedIds) {
        return withUpdatedIds.stream().map(c -> {
            Optional<C4Component> c4Component = c4Components.stream().filter(c4c -> c.getComponentId() != null && c4c.getId().equals(c.getComponentId().getId())).findFirst();
            return c4Component.map(component -> new TddContainerByComponent(c.getComponentId(), component.getPath().getPath(), c.isDeleted(), c.getTdds())).orElse(c);
        }).collect(toList());
    }

    private static Tdd tddWithFileName(TddComponentReference id, Tdd tdd) {
        if (id == null) return tdd;
        final var tddContent = tdd.getContent();
        if (null == tddContent) {
            return tdd;
        }

        Tdd tddWithFileName = new Tdd(tdd.getText(), tddContent.getFilename(), tddContent);
        return tddWithFileName;
    }

    public static ArchitectureUpdate annotateC4Paths(ArchitectureDataStructure dataStructure, ArchitectureUpdate au) {
        Set<C4Component> c4Components = dataStructure.getModel().getComponents();
        List<TddContainerByComponent> tddContainersByComponent = au.getTddContainersByComponent();

        List<TddContainerByComponent> withUpdatedIds = updateIdBasedOnPath(c4Components, tddContainersByComponent);

        List<TddContainerByComponent> withUpdatedPath = updatePathBasedOnId(c4Components, withUpdatedIds);

        return au.toBuilder().tddContainersByComponent(withUpdatedPath).build();
    }

    private static List<TddContainerByComponent> updateIdBasedOnPath(Set<C4Component> c4Components, List<TddContainerByComponent> tddContainersByComponent) {
        return tddContainersByComponent.stream().map(c -> {
            if (c.getComponentId() != null) {
                return c;
            }
            Optional<C4Component> c4Component = c4Components.stream().filter(c4c -> c4c.getPath().getPath().equals(c.getComponentPath())).findFirst();
            return c4Component.map(component -> new TddContainerByComponent(new TddComponentReference(component.getId()), c.getComponentPath(), c.isDeleted(), c.getTdds())).orElse(c);
        }).collect(toList());
    }

    public static ArchitectureUpdate annotateTddContentFiles(ArchitectureUpdate au) {
        var tddContainers = au.getTddContainersByComponent().stream()
                .map(c -> new TddContainerByComponent(
                                c.getComponentId(),
                                c.getComponentPath(),
                                c.isDeleted(),
                                c.getTdds().entrySet().stream().collect(toMap(
                                        Map.Entry::getKey,
                                        (tdd) -> tddWithFileName(c.getComponentId(), tdd.getValue())))
                        )
                ).collect(toList());

        return au.toBuilder().tddContainersByComponent(tddContainers).build();
    }

    public static boolean isComponentsEmpty(ArchitectureDataStructure dataStructure, ArchitectureUpdate au) {
        return au.getTddContainersByComponent().stream()
                .flatMap(tdd -> dataStructure
                        .getModel().getComponents().stream().filter(c ->
                                (tdd.getComponentId() != null && c.getId().equalsIgnoreCase(tdd.getComponentId().getId()))
                                        || c.getPath().getPath().equalsIgnoreCase(tdd.getComponentPath()))
                ).findAny()
                .isEmpty();
    }
}
