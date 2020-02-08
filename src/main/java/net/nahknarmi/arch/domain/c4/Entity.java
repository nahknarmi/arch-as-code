package net.nahknarmi.arch.domain.c4;

import java.util.List;

public interface Entity extends Relatable, Tagable {
    String getName();

    String getDescription();

    C4Path getPath();

    List<C4Tag> getTags();

    List<C4Relationship> getRelationships();
}
