package net.trilogy.arch.adapter.jira;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Find the differences between two data collections where there is a common
 * key to match them up.  This boils down to: <ol>
 * <li>Convert each data collection to a map by key</li>
 * <li>Look for disjoint keys (added items for the "ours" collection; removed
 * items for the "theirs" collection)</li>
 * <li>For common keys, use an "equivalence" function to find changed
 * items based on satellite data; keys have already been matched</li>
 * </ol>
 * The algorithm is: <ol>
 * <li>Find keys for left side, and for right side</li>
 * <li>Split the left side into 3 buckets based on key equality: <ul>
 * <li>New in the left side (added)</li>
 * <li>Missing from the left side (removed)</li>
 * <li>Common with the right side (potentially changed)</li>
 * </ul></li>
 * <li>For potentially changed, check <var>equivalent</var></li>
 * <li>Split the potentially changed into 2 buckets (the key has already been
 * checked above): <ul>
 * <li>No satellite data changes (unchanged)</li>
 * <li>Satellite data has changed (changed)</li>
 * </ul></li>
 * <li>Discard the "unchanged" bucket</li>
 * <li>Return the "added", "removed", and "changed" buckets</li>
 * </ol>
 *
 * @param <KEY> the common key between data types, eg, "AAC-129"
 * @param <OURS> our data type, ie, AaC's notion of JIRA stories
 * @param <THEIRS> their data type, ie, Atlassian's JIRA stories
 *
 * @todo Reinventing the wheel compared to other functional languages
 * @todo Research if one of our 3rd-party libraries provides this
 */
@RequiredArgsConstructor
public final class DifferenceEngine<KEY, OURS, THEIRS> {
    private final Function<OURS, KEY> ourCommonKey;
    private final Function<THEIRS, KEY> theirCommonKey;
    private final BiFunction<OURS, THEIRS, Boolean> equivalent;

    public Difference<KEY, OURS, THEIRS> diff(
            final Collection<OURS> ours,
            final Collection<THEIRS> theirs) {
        return new Difference<>(ourCommonKey, theirCommonKey, equivalent, ours, theirs);
    }

    public static final class Difference<KEY, OURS, THEIRS> {
        public final List<OURS> addedByUs;
        public final List<THEIRS> removedByUs;
        public final List<Pair<OURS, THEIRS>> changedByUs;

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
                    .collect(toUnmodifiableList());

            removedByUs = theirsByKey.entrySet().stream()
                    .filter(it -> !oursByKey.containsKey(it.getKey()))
                    .map(Entry::getValue)
                    .collect(toUnmodifiableList());

            // Avoid multiple map fetches by assuming theirs contains the key,
            // and ignoring if not
            changedByUs = oursByKey.entrySet().stream()
                    .map(it -> Pair.of(it.getValue(), theirsByKey.get(it.getKey())))
                    .filter(it -> {
                        final var them = it.getValue();
                        return null != them && !equivalent.apply(it.getKey(), them);
                    })
                    .collect(toUnmodifiableList());
        }
    }
}
