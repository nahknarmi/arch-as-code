package net.nahknarmi.arch.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class C4Relationship {
    private final Relatable from;
    private final Relatable to;
    private final RelationshipType relationshipType;
}
