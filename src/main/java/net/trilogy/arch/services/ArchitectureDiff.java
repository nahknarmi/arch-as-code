package net.trilogy.arch.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;

import java.util.Set;

@ToString
@EqualsAndHashCode
public class ArchitectureDiff {
    PeopleDiff peopleDiff;
    SystemsDiff systemsDiff;

    public ArchitectureDiff(PeopleDiff peopleDiff, SystemsDiff systemsDiff) {
        this.peopleDiff = peopleDiff;
        this.systemsDiff = systemsDiff;
    }

    public static ArchitectureDiff empty() {
        return new ArchitectureDiff(
                PeopleDiff.empty(),
                SystemsDiff.empty()
        );
    }

    @ToString
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PeopleDiff {
        private final Set<C4Person> first;
        private final Set<C4Person> second;
        private final Set<C4Person> both;

        public static PeopleDiff empty() {
            return new PeopleDiff(Set.of(), Set.of(), Set.of());
        }
    }

    @ToString
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SystemsDiff {
        private final Set<C4SoftwareSystem> first;
        private final Set<C4SoftwareSystem> second;
        private final Set<C4SoftwareSystem> both;

        public static SystemsDiff empty() {
            return new SystemsDiff(Set.of(), Set.of(), Set.of());
        }
    }
}
