package net.trilogy.arch.adapter.jira;

import lombok.Data;
import net.trilogy.arch.adapter.jira.DifferenceEngine.Pair;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DifferenceEngineTest {
    private final static List<Left> ours = List.of(
            new Left("ALICE", 2), // common, unchanged
            new Left("BOB", 3), // added
            new Left("DAVE", 5)); // common, changed
    private static final List<Right> theirs = List.of(
            new Right("ALICE", "2"), // common, unchanged
            new Right("CAROL", "4"), // removed
            new Right("DAVE", "6")); // common, changed

    private final DifferenceEngine<String, Left, Right> diff =
            new TestDifferenceEngine(ours, theirs);

    @Test
    public void find_additions() {
        assertEquals(Set.of(ours.get(1)), diff.addedByUs);
    }

    @Test
    public void find_removals() {
        assertEquals(Set.of(theirs.get(1)), diff.removedByUs);
    }

    @Test
    public void find_changes() {
        assertEquals(Set.of(Pair.of(ours.get(2), theirs.get(2))), diff.changedByUs);
    }

    @Data
    private static class Left {
        private final String key;
        private final int satelliteData;
    }

    @Data
    private static class Right {
        private final String key;
        private final String satelliteData;
    }

    private static class TestDifferenceEngine extends DifferenceEngine<String, Left, Right> {
        public TestDifferenceEngine(
                final List<Left> lefts,
                final List<Right> rights) {
            super(lefts, rights);
        }

        @Override
        public boolean equivalent(final Left us, final Right them) {
            return us.key.equals(them.key)
                    && String.valueOf(us.satelliteData).equals(them.satelliteData);
        }

        @Override
        public String keyFromUs(final Left us) {
            return us.key;
        }

        @Override
        public String keyFromThem(final Right them) {
            return them.key;
        }
    }
}
