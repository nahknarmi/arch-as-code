package net.nahknarmi.arch.domain.c4;

import java.util.List;

public interface Relatable {
    C4Path getPath();

    List<C4Relationship> getRelationships();
}
