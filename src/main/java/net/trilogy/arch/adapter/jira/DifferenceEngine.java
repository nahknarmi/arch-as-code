package net.trilogy.arch.adapter.jira;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

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
@RequiredArgsConstructor
public final class DifferenceEngine<KEY, OURS, THEIRS> {
    private final Function<OURS, KEY> ourCommonKey;
    private final Function<THEIRS, KEY> theirCommonKey;
    private final BiFunction<OURS, THEIRS, Boolean> equivalent;

    public Difference<KEY, OURS, THEIRS> diff(final Collection<OURS> ours, final Collection<THEIRS> theirs) {
        return new Difference<>(ourCommonKey, theirCommonKey, equivalent, ours, theirs);
    }

    public static final class Difference<KEY, OURS, THEIRS> {
        public final Set<OURS> addedByUs;
        public final Set<THEIRS> removedByUs;
        public final Set<Pair<OURS, THEIRS>> changedByUs;

        private Difference(
                final Function<OURS, KEY> ourCommonKey,
                final Function<THEIRS, KEY> theirCommonKey,
                final BiFunction<OURS, THEIRS, Boolean> equivalent,
                final Collection<OURS> ours,
                final Collection<THEIRS> theirs) {
            final var oursByKey = ours.stream()
                    .collect(toMap(ourCommonKey, identity()));
            final var theirsByKey = theirs.stream()
                    .collect(toMap(theirCommonKey, identity()));

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
                    .filter(it -> !equivalent.apply(it.getValue(), theirsByKey.get(it.getKey())))
                    .map(it -> Pair.of(it.getValue(), theirsByKey.get(it.getKey())))
                    .collect(toUnmodifiableSet());
        }
    }
}
