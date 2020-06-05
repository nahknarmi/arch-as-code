package net.trilogy.arch.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Diff<T> {
    final private String id;
    final private T before;
    final private T after;
    private Status status;

    public Diff(String id, T before, T after) {
        this.id = id;
        this.before = before;
        this.after = after;
        this.status = calculateStatus();

    }

    private Status calculateStatus() {
        if (before == null && after == null) throw new UnsupportedOperationException();
        if (before == null) return Status.CREATED;
        if (after == null) return Status.DELETED;
        if (before.equals(after)) return Status.NO_UPDATE;

        return Status.UPDATED;
    }

    public void markChildrenUpdated() {
        if (status.equals(Status.NO_UPDATE)) {
            this.status = Status.CHILDREN_UPDATED;
        }
    }

    public enum Status {
        CREATED,
        UPDATED,
        DELETED,
        CHILDREN_UPDATED,
        NO_UPDATE
    }
}
