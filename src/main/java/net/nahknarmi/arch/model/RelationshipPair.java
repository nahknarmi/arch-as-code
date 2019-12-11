package net.nahknarmi.arch.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class RelationshipPair {
    @NonNull
    private String name;
    @NonNull
    private String with;

    RelationshipPair() {
    }
}
