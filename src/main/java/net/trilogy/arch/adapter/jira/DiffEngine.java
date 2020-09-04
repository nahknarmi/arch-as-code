package net.trilogy.arch.adapter.jira;

import java.util.List;

/**
 * With restrictions on Java Generics and lack of union types, fall back to
 * throwing exceptions rather than returning result types.  Notice the
 * nuisance of checked exceptions poking its head in.
 */
public interface DiffEngine<KEY, OURS, THEIRS> {
    KEY commonIdFromUs(final OURS us) throws DiffException;

    KEY commonIdFromThem(final THEIRS them) throws DiffException;

    THEIRS asThem(final OURS us) throws DiffException;

    void addToThem(final THEIRS them) throws DiffException;

    void removeFromThem(final KEY commonId) throws DiffException;

    List<OURS> addedByUs() throws DiffException;

    List<OURS> removedByUs() throws DiffException;

    List<OURS> changedByUs() throws DiffException;

    default void add() {
        addedByUs().stream()
                .map(this::asThem)
                .forEach(this::addToThem);
    }

    default void remove() {
        removedByUs().stream()
                .map(this::commonIdFromUs)
                .forEach(this::removeFromThem);
    }

    default void change() {
        changedByUs().stream()
                .peek(it -> removeFromThem(commonIdFromUs(it)))
                .map(this::asThem)
                .forEach(this::addToThem);
    }

    final class DiffException extends RuntimeException {
    }
}
