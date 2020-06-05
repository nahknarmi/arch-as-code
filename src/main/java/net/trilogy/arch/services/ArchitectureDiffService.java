package net.trilogy.arch.services;

import com.google.common.collect.Streams;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.Diff;
import net.trilogy.arch.domain.Diffable;
import net.trilogy.arch.domain.c4.C4Person;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArchitectureDiffService {
    public static Set<Diff> diff(ArchitectureDataStructure firstArch, ArchitectureDataStructure secondArch) {
        final Set<Diff> firstDiffs = firstArch.getModel().allEntities().stream()
                .map(p1 -> new Diff(
                        p1.getId(),
                        p1,
                        secondArch.getModel().findEntityById(p1.getId()).orElse(null)
                )).collect(Collectors.toSet());

        final Set<Diff> secondDiffs = secondArch.getModel().getPeople().stream()
                .map(p2 -> new Diff(
                        p2.getId(),
                        (C4Person) firstArch.getModel().findEntityById(p2.getId()).orElse(null),
                        p2
                )).collect(Collectors.toSet());

        return union(firstDiffs, secondDiffs).collect(Collectors.toSet());
    }

    private static <T> Stream<T> union(Set<T> first, Set<T> second) {
        return Streams.concat(first.stream(), second.stream());
    }
}
