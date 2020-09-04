package net.trilogy.arch.adapter.jira;

import lombok.Data;
import net.trilogy.arch.adapter.jira.DifferenceEngine.Difference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DifferenceEngineTest {
    private static final List<Left> ours = List.of(
            new Left("ALICE", 2), // common, unchanged
            new Left("BOB", 3), // added
            new Left("DAVE", 5)); // common, changed
    private static final List<Right> theirs = List.of(
            new Right("ALICE", "2"), // common, unchanged
            new Right("CAROL", "4"), // removed
            new Right("DAVE", "6")); // common, changed

    private static final DifferenceEngine<String, Left, Right> engine =
            new DifferenceEngine<>(it -> it.key,
                    it -> it.key,
                    (a, b) -> a.key.equals(b.key)
                            && String.valueOf(a.satelliteData).equals(b.satelliteData));
    private static final Difference<String, Left, Right> diff = engine.diff(ours, theirs);

    @Test
    public void find_additions() {
        assertEquals(List.of(ours.get(1)), diff.addedByUs);
    }

    @Test
    public void find_removals() {
        assertEquals(List.of(theirs.get(1)), diff.removedByUs);
    }

    @Test
    public void find_changes() {
        assertEquals(List.of(Pair.of(ours.get(2), theirs.get(2))), diff.changedByUs);
    }

    @Data
    private static class Left {
        public final String key;
        public final int satelliteData;
    }

    @Data
    private static class Right {
        public final String key;
        public final String satelliteData;
    }
}
