package net.trilogy.arch.adapter.jira;

import lombok.Data;
import lombok.Getter;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * With restrictions on Java Generics and lack of union types, fall back to
 * throwing exceptions rather than returning result types.  Notice the
 * nuisance of checked exceptions poking its head in.
 */
public abstract class DifferenceEngine<KEY, OURS, THEIRS> {
    public abstract boolean equivalent(final OURS us, final THEIRS them);

    public abstract KEY keyFromUs(final OURS us);

    public abstract KEY keyFromThem(final THEIRS them);

    public final Set<OURS> addedByUs;
    public final Set<THEIRS> removedByUs;
    public final Set<Pair<OURS, THEIRS>> changedByUs;

    public DifferenceEngine(final Collection<OURS> ours, final Collection<THEIRS> theirs) {
        final var oursByKey = ours.stream()
                .collect(toMap(this::keyFromUs, identity()));
        final var theirsByKey = theirs.stream()
                .collect(toMap(this::keyFromThem, identity()));

        addedByUs = oursByKey.entrySet().stream()
                .filter(it -> !theirsByKey.containsKey(it.getKey()))
                .map(Entry::getValue)
                .collect(toSet());

        removedByUs = theirsByKey.entrySet().stream()
                .filter(it -> !oursByKey.containsKey(it.getKey()))
                .map(Entry::getValue)
                .collect(toSet());

        changedByUs = oursByKey.entrySet().stream()
                .filter(it -> theirsByKey.containsKey(it.getKey()))
                .filter(it -> !equivalent(it.getValue(), theirsByKey.get(it.getKey())))
                .map(it -> Pair.of(it.getValue(), theirsByKey.get(it.getKey())))
                .collect(toSet());
    }

    @Data(staticConstructor = "of")
    public static class Pair<T, U> {
        public final T first;
        public final U second;
    }
}
