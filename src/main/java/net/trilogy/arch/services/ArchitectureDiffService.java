package net.trilogy.arch.services;

import com.google.common.collect.Sets;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.Diff;

import java.util.Set;
import java.util.stream.Collectors;

public class ArchitectureDiffService {
    public static Set<Diff> diff(ArchitectureDataStructure firstArch, ArchitectureDataStructure secondArch) {
        final Set<Diff> firstDiffs1 = firstArch.getModel().allEntities().stream()
                .map(p1 -> new Diff(
                        p1.getId(),
                        p1,
                        secondArch.getModel().findEntityById(p1.getId()).orElse(null)
                )).collect(Collectors.toSet());

        final Set<Diff> secondDiffs1 = secondArch.getModel().allEntities().stream()
                .map(p2 -> new Diff(
                        p2.getId(),
                        firstArch.getModel().findEntityById(p2.getId()).orElse(null),
                        p2
                )).collect(Collectors.toSet());

        final var entityDiffs = Sets.union(firstDiffs1, secondDiffs1);

        final Set<Diff> firstDiffs2 = firstArch.getModel().allRelationships().stream()
                .map(t -> t._2)
                .map(r1 -> {
                    String id = r1.getId();
                    return new Diff(
                            r1.getId(),
                            r1,
                            secondArch.getModel().findRelationshipById(id).orElse(null)
                    );
                }).collect(Collectors.toSet());

        final Set<Diff> secondDiffs2 = secondArch.getModel().allRelationships().stream()
                .map(t -> t._2)
                .map(r2 -> {
                    String id = r2.getId();
                    return new Diff(
                            r2.getId(),
                            firstArch.getModel().findRelationshipById(id).orElse(null),
                            r2
                    );
                }).collect(Collectors.toSet());

        final var relationshipDiffs = Sets.union(firstDiffs2, secondDiffs2);

        return Sets.union(entityDiffs, relationshipDiffs);
    }
}
