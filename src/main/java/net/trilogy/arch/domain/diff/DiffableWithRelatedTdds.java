package net.trilogy.arch.domain.diff;

import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;

import java.util.HashMap;
import java.util.Map;

public abstract class DiffableWithRelatedTdds {
    private Map<TddId, Tdd> relatedTdds = new HashMap<>();

    public String[] getRelatedTddsText() {
        return relatedTdds.entrySet().stream()
                .map(e -> e.getKey() + " - " + e.getValue().getDetails())
                .toArray(String[]::new);
    }

    public void setRelatedTdds(Map<TddId, Tdd> relatedTo) {
        relatedTdds = relatedTo;
    }

    public boolean hasRelatedTdds() {
        return relatedTdds != null && !relatedTdds.isEmpty();
    }
}
