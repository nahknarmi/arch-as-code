package net.trilogy.arch.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.trilogy.arch.domain.c4.C4Person;

import java.util.Set;

@ToString
@EqualsAndHashCode
public class ArchitectureDiff {
    PeopleDiff peopleDiff;

    public ArchitectureDiff(PeopleDiff peopleDiff) {
        this.peopleDiff = peopleDiff;
    }

    public static ArchitectureDiff empty() {
        return new ArchitectureDiff(
                new PeopleDiff(Set.of(), Set.of(), Set.of())
        );
    }

    @ToString
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PeopleDiff {
        private final Set<C4Person> first;
        private final Set<C4Person> second;
        private final Set<C4Person> both;
    }
}
