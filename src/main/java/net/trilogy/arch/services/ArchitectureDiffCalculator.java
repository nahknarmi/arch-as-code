package net.trilogy.arch.services;

import com.google.common.collect.Sets;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Container;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffSet;
import net.trilogy.arch.domain.diff.Diffable;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.domain.diff.DiffableRelationship;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArchitectureDiffCalculator {
    public static DiffSet architectureDiff(ArchitectureDataStructure beforeArch, ArchitectureDataStructure afterArch) {
        final Set<Diff> firstDiffs = getAllDiffables(beforeArch).stream()
                .map(diffableInFirst -> {
                            var diffableInSecond = findById(afterArch, diffableInFirst.getId()).orElse(null);
                            return makeDiff(beforeArch, afterArch, diffableInFirst, diffableInSecond);
                        }
                )
                .collect(Collectors.toSet());

        final Set<Diff> secondDiffs = getAllDiffables(afterArch).stream()
                .map(thing2 -> {
                            var thing1 = findById(beforeArch, thing2.getId()).orElse(null);
                            return makeDiff(beforeArch, afterArch, thing1, thing2);
                        }
                )
                .collect(Collectors.toSet());

        return new DiffSet(Sets.union(firstDiffs, secondDiffs));
    }

    private static Diff makeDiff(ArchitectureDataStructure firstArch,
                                 ArchitectureDataStructure secondArch,
                                 Diffable diffableInFirst,
                                 Diffable diffableInSecond) {
        return new Diff(
                diffableInFirst, getDescendants(diffableInFirst, firstArch),
                diffableInSecond, getDescendants(diffableInSecond, secondArch)
        );
    }

    private static Set<Diffable> getDescendants(Diffable diffable, ArchitectureDataStructure arch) {
        return getDescendants(diffable, arch, false);
    }

    private static Set<Diffable> getDescendants(Diffable diffable, ArchitectureDataStructure arch, boolean shouldAddRelationships) {
        if (!(diffable instanceof DiffableEntity)) {
            return Set.of();
        }
        final var entity = ((DiffableEntity) diffable).getEntity();
        final var results = new HashSet<Diffable>();

        if (shouldAddRelationships) {
            results.addAll(
                    entity.getRelationships()
                            .stream()
                            .map(it -> new DiffableRelationship(entity, it))
                            .collect(Collectors.toList())
            );
        }

        if (entity instanceof C4SoftwareSystem) {
            Set<C4Container> containers = getContainers((C4SoftwareSystem) entity, arch);
            for (C4Container container : containers) {
                results.add(new DiffableEntity(container));
                results.addAll(getDescendants(new DiffableEntity(container), arch, true));
            }
        }
        if (entity instanceof C4Container) {
            Set<C4Component> components = getComponents((C4Container) entity, arch);
            for (C4Component component : components) {
                results.add(new DiffableEntity(component));
                results.addAll(getDescendants(new DiffableEntity(component), arch, true));
            }
        }
        return results;
    }

    private static Set<C4Container> getContainers(C4SoftwareSystem system, ArchitectureDataStructure arch) {
        return arch.getModel().getContainers().stream().filter(it -> Objects.equals(it.getSystemId(), system.getId())).collect(Collectors.toSet());
    }

    private static Set<C4Component> getComponents(C4Container container, ArchitectureDataStructure arch) {
        return arch.getModel().getComponents().stream().filter(it -> Objects.equals(it.getContainerId(), container.getId())).collect(Collectors.toSet());
    }

    private static Optional<? extends Diffable> findById(ArchitectureDataStructure arch, String id) {
        return Stream.of(
                arch.getModel().findRelationshipById(id).map(it -> new DiffableRelationship(arch, it)),
                arch.getModel()
                        .findEntityById(id)
                        .map(DiffableEntity::new)
        )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    private static Set<Diffable> getAllDiffables(ArchitectureDataStructure arch) {
        return Sets.union(
                arch.getModel()
                        .allEntities()
                        .stream()
                        .map(DiffableEntity::new)
                        .collect(Collectors.toSet()),

                arch.getModel()
                        .allRelationships()
                        .stream()
                        .map(entityRelationshipPair -> new DiffableRelationship(entityRelationshipPair._1, entityRelationshipPair._2))
                        .collect(Collectors.toSet())
        );
    }
}
