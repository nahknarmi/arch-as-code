package net.trilogy.arch.annotators;

import lombok.experimental.UtilityClass;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTddContainerByComponent;
import net.trilogy.arch.domain.c4.C4Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@UtilityClass
public class ArchitectureUpdateAnnotator {
    private static List<YamlTddContainerByComponent> updatePathBasedOnId(Set<C4Component> c4Components, List<YamlTddContainerByComponent> withUpdatedIds) {
        return withUpdatedIds.stream().map(c -> c4Components.stream()
                .filter(c4c -> c.getComponentId() != null && c4c.getId().equals(c.getComponentId().getId()))
                .findFirst()
                .map(component -> new YamlTddContainerByComponent(c.getComponentId(), component.getPath().getPath(), c.isDeleted(), c.getTdds()))
                .orElse(c))
                .collect(toList());
    }

    private static YamlTdd tddWithFileName(TddComponentReference id, YamlTdd tdd) {
        if (id == null) return tdd;
        final var tddContent = tdd.getContent();
        if (null == tddContent) {
            return tdd;
        }

        return tdd.withContent(tddContent);
    }

    public static YamlArchitectureUpdate annotateC4Paths(ArchitectureDataStructure dataStructure, YamlArchitectureUpdate au) {
        Set<C4Component> c4Components = dataStructure.getModel().getComponents();
        List<YamlTddContainerByComponent> tddContainersByComponent = au.getTddContainersByComponent();

        List<YamlTddContainerByComponent> withUpdatedIds = updateIdBasedOnPath(c4Components, tddContainersByComponent);

        List<YamlTddContainerByComponent> withUpdatedPath = updatePathBasedOnId(c4Components, withUpdatedIds);

        return au.toBuilder().tddContainersByComponent(withUpdatedPath).build();
    }

    private static List<YamlTddContainerByComponent> updateIdBasedOnPath(Set<C4Component> c4Components, List<YamlTddContainerByComponent> tddContainersByComponent) {
        return tddContainersByComponent.stream().map(c -> {
            if (c.getComponentId() != null) {
                return c;
            }
            return c4Components.stream()
                    .filter(c4c -> c4c.getPath().getPath().equals(c.getComponentPath()))
                    .findFirst()
                    .map(component -> new YamlTddContainerByComponent(new TddComponentReference(component.getId()), c.getComponentPath(), c.isDeleted(), c.getTdds()))
                    .orElse(c);
        })
                .collect(toList());
    }

    public static YamlArchitectureUpdate annotateTddContentFiles(YamlArchitectureUpdate au) {
        var tddContainers = au.getTddContainersByComponent().stream()
                .map(c -> new YamlTddContainerByComponent(
                        c.getComponentId(),
                        c.getComponentPath(),
                        c.isDeleted(),
                        c.getTdds().entrySet().stream().collect(toMap(
                                Map.Entry::getKey,
                                (tdd) -> tddWithFileName(c.getComponentId(), tdd.getValue())))))
                .collect(toList());

        return au.toBuilder().tddContainersByComponent(tddContainers).build();
    }

    public static boolean isComponentsEmpty(ArchitectureDataStructure dataStructure, YamlArchitectureUpdate au) {
        return au.getTddContainersByComponent().stream()
                .flatMap(tdd -> dataStructure
                        .getModel().getComponents().stream().filter(c ->
                                (tdd.getComponentId() != null && c.getId().equalsIgnoreCase(tdd.getComponentId().getId()))
                                        || c.getPath().getPath().equalsIgnoreCase(tdd.getComponentPath())))
                .findAny()
                .isEmpty();
    }
}
