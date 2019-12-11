package net.nahknarmi.arch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
public class C4Person implements Relatable {
    @NonNull
    private String name;
    @NonNull
    private String description;

    private List<RelationshipPair> relationships = emptyList();

    C4Person() {
    }

    @Override
    public List<RelationshipPair> relations() {
        return relationships;
    }
}
