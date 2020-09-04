package net.trilogy.arch.adapter.jira;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * With restrictions on Java Generics and lack of union types, fall back to
 * throwing exceptions rather than returning result types.  Notice the
 * nuisance of checked exceptions poking its head in.
 */
public interface DiffEngine<PARENT_KEY, CHILD_KEY, OURS, THEIRS> {
    boolean equivalent(final OURS us, final THEIRS them) throws DiffException;

    List<THEIRS> whatAreTheirs(final PARENT_KEY key) throws DiffException;

    /** @todo Verify "ours" is for the correct parent key */
    PARENT_KEY parentKeyFromUs(final OURS us) throws DiffException;

    CHILD_KEY childKeyFromUs(final OURS us) throws DiffException;

    PARENT_KEY parentKeyFromThem(final THEIRS them) throws DiffException;

    CHILD_KEY childKeyFromThem(final THEIRS them) throws DiffException;

    THEIRS asThem(final OURS us) throws DiffException;

    void addToThem(final THEIRS them) throws DiffException;

    void removeFromThem(final CHILD_KEY childKey) throws DiffException;

    default List<OURS> addedByUs(final PARENT_KEY parentKey, List<OURS> ours) throws DiffException {
        final var theirChildKeys = whatAreTheirs(parentKey).stream()
                .map(this::childKeyFromThem)
                .collect(toSet());
        return ours.stream()
                .filter(it -> theirChildKeys.contains(childKeyFromUs(it)))
                .collect(toList());
    }

    List<OURS> removedByUs() throws DiffException;

    List<OURS> changedByUs() throws DiffException;

    default void addIfNeeded(final PARENT_KEY parentKey, final List<OURS> ours) {
        addedByUs(parentKey, ours).stream()
                .map(this::asThem)
                .forEach(this::addToThem);
    }

    default void remove() {
        removedByUs().stream()
                .map(this::childKeyFromUs)
                .forEach(this::removeFromThem);
    }

    default void change() {
        changedByUs().stream()
                .peek(it -> removeFromThem(childKeyFromUs(it)))
                .map(this::asThem)
                // TODO: This resets the remote JIRA card ID :(
                .forEach(this::addToThem);
    }

    final class DiffException extends RuntimeException {
    }
}
