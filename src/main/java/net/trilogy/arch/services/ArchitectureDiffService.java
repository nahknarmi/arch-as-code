package net.trilogy.arch.services;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Person;

import java.util.Set;

public class ArchitectureDiffService {
    public static ArchitectureDiff diff(ArchitectureDataStructure first, ArchitectureDataStructure second) {
        ArchitectureDiff.PeopleDiff peopleDiff = calcPeopleDiff(first, second);

        return new ArchitectureDiff(peopleDiff);
    }

    private static ArchitectureDiff.PeopleDiff calcPeopleDiff(ArchitectureDataStructure first,
                                                              ArchitectureDataStructure second) {
        Set<C4Person> inFirst = first.getModel().getPeople();
        Set<C4Person> inSecond = second.getModel().getPeople();
        final ImmutableSet<C4Person> onlyInFirst = Sets.difference(inFirst, inSecond).immutableCopy();
        final ImmutableSet<C4Person> onlyInSecond = Sets.difference(inSecond, inFirst).immutableCopy();
        final ImmutableSet<C4Person> inBoth = Sets.intersection(inFirst, inSecond).immutableCopy();

        return new ArchitectureDiff.PeopleDiff(onlyInFirst, onlyInSecond, inBoth);
    }
}
