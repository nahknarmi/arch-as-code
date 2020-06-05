package net.trilogy.arch.domain;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class DiffTest {

    @Test
    public void shouldCalculateCreatedStatus() {
        final Diff<String> diff = new Diff<>(null, null, "toBeCreated");
        assertThat(diff.getStatus(), equalTo(Diff.Status.CREATED));
    }

    @Test
    public void shouldCalculateDeletedStatus() {
        final Diff<String> diff = new Diff<>(null, "toBeDeleted", null);
        assertThat(diff.getStatus(), equalTo(Diff.Status.DELETED));
    }

    @Test
    public void shouldCalculateUpdatedStatus() {
        final Diff<String> diff = new Diff<>(null, "toBeUpdated", "updated");
        assertThat(diff.getStatus(), equalTo(Diff.Status.UPDATED));
    }

    @Test
    public void shouldCalculateNoUpdateStatus() {
        final Diff<String> diff = new Diff<>(null, "noChange", "noChange");
        assertThat(diff.getStatus(), equalTo(Diff.Status.NO_UPDATE));
    }

    @Test
    public void shouldCalculatedChildrenUpdated() {
        final Diff<String> diff = new Diff<>(null, "noUpdate", "noUpdate");
        diff.markChildrenUpdated();
        assertThat(diff.getStatus(), equalTo(Diff.Status.CHILDREN_UPDATED));
    }

    @Test
    public void shouldNotCalculateChildrenIfChangedStatus() {
        final Diff<String> created = new Diff<>(null, null, "toBeCreated");
        final Diff<String> deleted = new Diff<>(null, "toBeDeleted", null);
        final Diff<String> updated = new Diff<>(null, "toBeUpdated", "updated");
        created.markChildrenUpdated();
        deleted.markChildrenUpdated();
        updated.markChildrenUpdated();

        assertThat(created.getStatus(), equalTo(Diff.Status.CREATED));
        assertThat(deleted.getStatus(), equalTo(Diff.Status.DELETED));
        assertThat(updated.getStatus(), equalTo(Diff.Status.UPDATED));
    }
}
