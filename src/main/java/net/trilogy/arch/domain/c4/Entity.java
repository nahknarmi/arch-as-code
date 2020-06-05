package net.trilogy.arch.domain.c4;

import java.util.List;

import net.trilogy.arch.domain.Diffable;

public interface Entity extends Diffable, HasRelation, HasTag, HasIdentity {
    String getId();

    String getAlias();

    String getName();

    String getDescription();

    C4Type getType();

    C4Path getPath();

    List<C4Relationship> getRelationships();
}
