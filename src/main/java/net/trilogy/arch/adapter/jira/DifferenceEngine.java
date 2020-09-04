package net.trilogy.arch.adapter.jira;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Find the differences between two data collections where there is a common
 * key to match them up.
 *
 * @param <KEY> the common key between data types of ours and theirs
 * @param <OURS> our data type, ie, AaC
 * @param <THEIRS> their data type, eg, JIRA and others
 *
 * @todo Reinventing the wheel compared to other functional languages
 * @todo Research if one our 3rd-party libraries provides this
 */
public abstract class DifferenceEngine<KEY, OURS, THEIRS> {
    public abstract boolean equivalent(final OURS us, final THEIRS them);

    public abstract KEY ourCommonKey(final OURS us);

    public abstract KEY theirCommonKey(final THEIRS them);

    public final Set<OURS> addedByUs;
    public final Set<THEIRS> removedByUs;
    public final Set<Pair<OURS, THEIRS>> changedByUs;

    public DifferenceEngine(final Collection<OURS> ours, final Collection<THEIRS> theirs) {
        final var oursByKey = ours.stream()
                .collect(toMap(this::ourCommonKey, identity()));
        final var theirsByKey = theirs.stream()
                .collect(toMap(this::theirCommonKey, identity()));

        addedByUs = oursByKey.entrySet().stream()
                .filter(it -> !theirsByKey.containsKey(it.getKey()))
                .map(Entry::getValue)
                .collect(toUnmodifiableSet());

        removedByUs = theirsByKey.entrySet().stream()
                .filter(it -> !oursByKey.containsKey(it.getKey()))
                .map(Entry::getValue)
                .collect(toUnmodifiableSet());

        changedByUs = oursByKey.entrySet().stream()
                .filter(it -> theirsByKey.containsKey(it.getKey()))
                .filter(it -> !equivalent(it.getValue(), theirsByKey.get(it.getKey())))
                .map(it -> Pair.of(it.getValue(), theirsByKey.get(it.getKey())))
                .collect(toUnmodifiableSet());
    }
}
