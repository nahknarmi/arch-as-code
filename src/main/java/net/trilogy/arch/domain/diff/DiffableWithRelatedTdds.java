package net.trilogy.arch.domain.diff;

import net.trilogy.arch.domain.architectureUpdate.Tdd;

import java.util.Map;

public class DiffableWithRelatedTdds {

    private Map<Tdd.Id, Tdd> relatedTdds;

    public String[] getRelatedTddsText() {
        return relatedTdds.entrySet().stream().map(e -> e.getKey() + " - " + e.getValue().getDetails()).toArray(String[]::new);
    }

    public void setRelatedTdds(Map<Tdd.Id, Tdd> relatedTo) {
        relatedTdds = relatedTo;
    }

    public boolean hasRelatedTdds() {
        return relatedTdds != null && ! relatedTdds.isEmpty();
    }
}
