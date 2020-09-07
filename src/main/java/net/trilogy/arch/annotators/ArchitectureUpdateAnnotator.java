package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.domain.c4.C4Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ArchitectureUpdateAnnotator {
    public ArchitectureUpdate annotateC4Paths(ArchitectureDataStructure dataStructure, ArchitectureUpdate au) {
        Set<C4Component> c4Components = dataStructure.getModel().getComponents();
        List<TddContainerByComponent> tddContainersByComponent = au.getTddContainersByComponent();

        List<TddContainerByComponent> withUpdatedIds = updateIdBasedOnPath(c4Components, tddContainersByComponent);

        List<TddContainerByComponent> withUpdatedPath = updatePathBasedOnId(c4Components, withUpdatedIds);

        return au.toBuilder().tddContainersByComponent(withUpdatedPath).build();
    }

    private List<TddContainerByComponent> updatePathBasedOnId(Set<C4Component> c4Components, List<TddContainerByComponent> withUpdatedIds) {
        return withUpdatedIds.stream().map(c -> {
            Optional<C4Component> c4Component = c4Components.stream().filter(c4c -> c.getComponentId() != null && c4c.getId().equals(c.getComponentId().getId())).findFirst();
            return c4Component.map(component -> new TddContainerByComponent(c.getComponentId(), component.getPath().getPath(), c.isDeleted(), c.getTdds())).orElse(c);
        }).collect(toList());
    }

    private List<TddContainerByComponent> updateIdBasedOnPath(Set<C4Component> c4Components, List<TddContainerByComponent> tddContainersByComponent) {
        return tddContainersByComponent.stream().map(c -> {
            if (c.getComponentId() != null) {
                return c;
            }
            Optional<C4Component> c4Component = c4Components.stream().filter(c4c -> c4c.getPath().getPath().equals(c.getComponentPath())).findFirst();
            return c4Component.map(component -> new TddContainerByComponent(new TddComponentReference(component.getId()), c.getComponentPath(), c.isDeleted(), c.getTdds())).orElse(c);
        }).collect(toList());
    }

    public ArchitectureUpdate annotateTddContentFiles(ArchitectureUpdate au) {
        var tddContainers = au.getTddContainersByComponent().stream()
                .map(c -> new TddContainerByComponent(
                                c.getComponentId(),
                                c.getComponentPath(),
                                c.isDeleted(),
                                c.getTdds().entrySet().stream().collect(toMap(
                                        Map.Entry::getKey,
                                        (tdd) -> addFileNameToTdd(c.getComponentId(), tdd, au.getTddContents())))
                        )
                ).collect(toList());

        return au.toBuilder().tddContainersByComponent(tddContainers).build();
    }

    public boolean isComponentsEmpty(ArchitectureDataStructure dataStructure, ArchitectureUpdate au) {
        return au.getTddContainersByComponent().stream()
                .flatMap(tdd -> dataStructure
                        .getModel().getComponents().stream().filter(c ->
                                (tdd.getComponentId() != null && c.getId().equalsIgnoreCase(tdd.getComponentId().getId()))
                                        || c.getPath().getPath().equalsIgnoreCase(tdd.getComponentPath()))
                ).findAny()
                .isEmpty();
    }

    private Tdd addFileNameToTdd(TddComponentReference id, Map.Entry<TddId, Tdd> pair, List<TddContent> tddContents) {
        String tddId = pair.getKey().toString();
        Tdd tdd = pair.getValue();
        if (id == null) {
            return tdd;
        }
        Optional<TddContent> found = tddContents.stream()
                .filter(tc -> tc.getComponentId().equals(id.getId()))
                .filter(tc -> tc.getTdd().equals(tddId))
                .findFirst();

        return found.map(tddContent -> new Tdd(tdd.getText(), tddContent.getFilename())).orElse(tdd);
    }
}
