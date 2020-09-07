package net.trilogy.arch.domain.diff;

import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.c4.C4Type;

import java.util.Map;

public interface Diffable {
    C4Type getType();

    String getId();

    String getName();

    String[] getRelatedTddsText();

    void setRelatedTdds(Map<TddId, Tdd> relatedTo);

    boolean hasRelatedTdds();

    boolean equals(Object o);
}
