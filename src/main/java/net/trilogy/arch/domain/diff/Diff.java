package net.trilogy.arch.domain.diff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

@EqualsAndHashCode
public class Diff {
    private final Diffable before;
    private final Diffable after;
    private final Set<? extends Diffable> descendantsBefore;
    private final Set<? extends Diffable> descendantsAfter;
    @Getter
    private final Status status;

    public Diff(Diffable before, Diffable after) {
        this.before = before;
        this.after = after;
        descendantsAfter = Set.of();
        descendantsBefore = Set.of();
        status = calculateStatus();
    }

    public Diff(Diffable before, Set<? extends Diffable> descendantsBefore, Diffable after, Set<? extends Diffable> descendantsAfter) {
        this.before = before;
        this.after = after;
        this.descendantsBefore = descendantsBefore;
        this.descendantsAfter = descendantsAfter;
        status = calculateStatus();
    }

    @Override
    public String toString() {
        String marker;
        if (status.equals(Status.UPDATED)) marker = "*";
        else if (status.equals(Status.NO_UPDATE_BUT_CHILDREN_UPDATED))
            marker = "~";
        else if (status.equals(Status.CREATED)) marker = "+";
        else if (status.equals(Status.DELETED)) marker = "-";
        else marker = "=";

        String id;
        if (before == null) {
            id = after.getId();
        } else {
            id = before.getId();
        }

        return marker + id;
    }

    private Status calculateStatus() {
        if (before == null && after == null)
            throw new IllegalArgumentException(
                    "Can't create Diff if states 'before' and 'after' are both null."
            );
        if (before == null) return Status.CREATED;
        if (after == null) return Status.DELETED;
        if (!before.equals(after)) return Status.UPDATED;
        if (!descendantsBefore.equals(descendantsAfter))
            return Status.NO_UPDATE_BUT_CHILDREN_UPDATED;
        return Status.NO_UPDATE;
    }

    public Diffable getElement() {
        return after != null ? after : before;
    }

    public Set<? extends Diffable> getDescendants() {
        return after != null ? descendantsAfter : descendantsBefore;
    }

    public enum Status {
        CREATED,
        UPDATED,
        DELETED,
        NO_UPDATE_BUT_CHILDREN_UPDATED,
        NO_UPDATE
    }
}
