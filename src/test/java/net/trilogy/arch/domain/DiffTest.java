package net.trilogy.arch.domain;

import org.junit.Test;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class DiffTest {

    @Test
    public void shouldCalculateCreatedStatus() {
        final Diff diff = new Diff(null, null, new Thing("toBeCreated"));
        assertThat(diff.getStatus(), equalTo(Diff.Status.CREATED));
    }

    @Test
    public void shouldCalculateDeletedStatus() {
        final Diff diff = new Diff(null, new Thing("toBeDeleted"), null);
        assertThat(diff.getStatus(), equalTo(Diff.Status.DELETED));
    }

    @Test
    public void shouldCalculateUpdatedStatus() {
        final Diff diff = new Diff(null, new Thing("toBeUpdated"), new Thing("updated"));
        assertThat(diff.getStatus(), equalTo(Diff.Status.UPDATED));
    }

    @Test
    public void shouldCalculateNoUpdateStatus() {
        final Diff diff = new Diff(null, new Thing("noChange"), new Thing("noChange"));
        assertThat(diff.getStatus(), equalTo(Diff.Status.NO_UPDATE));
    }

    @Test
    public void shouldCalculatedChildrenUpdated() {
        final Diff diff = new Diff(null, new Thing("noUpdate"), new Thing("noUpdate"));
        diff.markChildrenUpdated();
        assertThat(diff.getStatus(), equalTo(Diff.Status.NO_UPDATE_BUT_CHILDREN_UPDATED));
    }

    @Test
    public void shouldNotCalculateChildrenIfChangedStatus() {
        final Diff created = new Diff(null, null, new Thing("toBeCreated"));
        final Diff deleted = new Diff(null, new Thing("toBeDeleted"), null);
        final Diff updated = new Diff(null, new Thing("toBeUpdated"), new Thing("updated"));
        created.markChildrenUpdated();
        deleted.markChildrenUpdated();
        updated.markChildrenUpdated();

        assertThat(created.getStatus(), equalTo(Diff.Status.CREATED));
        assertThat(deleted.getStatus(), equalTo(Diff.Status.DELETED));
        assertThat(updated.getStatus(), equalTo(Diff.Status.UPDATED));
    }

    @EqualsAndHashCode
    @Getter
    private static class Thing implements Diffable {
        private final String id;
        public Thing(String id) {
            this.id = id;
        }
    }
}
