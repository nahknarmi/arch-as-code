package net.nahknarmi.arch.domain.c4;

import java.util.List;

public interface Relatable {
    String getName();

    List<Relationship> getRelationships();

    String getDescription();
}
