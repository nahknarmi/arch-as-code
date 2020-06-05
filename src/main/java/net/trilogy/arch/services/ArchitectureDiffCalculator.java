package net.trilogy.arch.services;

import com.google.common.collect.Sets;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.Diff;
import net.trilogy.arch.domain.Diffable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArchitectureDiffCalculator {
    public static Set<Diff> diff(ArchitectureDataStructure firstArch, ArchitectureDataStructure secondArch) {
        final Set<Diff> firstDiffs = getAllThings(firstArch).stream()
                .map(p1 -> new Diff(
                        p1.getId(),
                        p1,
                        findById(secondArch, p1.getId()).orElse(null)
                    )
                )
                .collect(Collectors.toSet());

        final Set<Diff> secondDiffs = getAllThings(secondArch).stream()
                .map(p2 -> new Diff(
                        p2.getId(),
                        findById(firstArch, p2.getId()).orElse(null),
                        p2
                    )
                )
                .collect(Collectors.toSet());

        return Sets.union(firstDiffs, secondDiffs);
    }

    private static Optional<? extends Diffable> findById(ArchitectureDataStructure arch, String id) {
        return Stream.of(
                arch.getModel().findRelationshipById(id),
                arch.getModel().findEntityById(id)
            )
            .filter(it -> it.isPresent())
            .findAny()
            .orElseGet(() -> Optional.empty());
    }

    private static Set<Diffable> getAllThings(ArchitectureDataStructure arch) {
        return Sets.union(
                arch.getModel().allEntities(),
                arch.getModel().allRelationships().stream().map(it -> it._2).collect(Collectors.toSet())
        );
    }
}
