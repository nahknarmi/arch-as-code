package net.trilogy.arch.services;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;

import java.util.Set;

public class ArchitectureDiffService {
    public static ArchitectureDiff diff(ArchitectureDataStructure first, ArchitectureDataStructure second) {
        ArchitectureDiff.PeopleDiff peopleDiff = calcPeopleDiff(first, second);
        ArchitectureDiff.SystemsDiff systemsDiff = calcSystemsDiff(first, second);

        return new ArchitectureDiff(peopleDiff, systemsDiff);
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

    private static ArchitectureDiff.SystemsDiff calcSystemsDiff(ArchitectureDataStructure first,
                                                                ArchitectureDataStructure second) {
        Set<C4SoftwareSystem> inFirst = first.getModel().getSystems();
        Set<C4SoftwareSystem> inSecond = second.getModel().getSystems();
        final ImmutableSet<C4SoftwareSystem> onlyInFirst = Sets.difference(inFirst, inSecond).immutableCopy();
        final ImmutableSet<C4SoftwareSystem> onlyInSecond = Sets.difference(inSecond, inFirst).immutableCopy();
        final ImmutableSet<C4SoftwareSystem> inBoth = Sets.intersection(inFirst, inSecond).immutableCopy();

        return new ArchitectureDiff.SystemsDiff(onlyInFirst, onlyInSecond, inBoth);
    }
}
