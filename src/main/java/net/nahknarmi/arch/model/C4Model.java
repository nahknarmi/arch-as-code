package net.nahknarmi.arch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
public class C4Model {
    @NonNull
    private List<C4Person> persons = emptyList();
    @NonNull
    private List<C4SoftwareSystem> systems = emptyList();

    C4Model() {
    }

    public List<C4Relationship> relationships() {
        return persons.stream().flatMap(from -> from.getRelationships().stream().map(rp -> {
            Relatable to = systems
                    .stream()
                    .filter(x -> x.getName().equals(rp.getWith()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to find system or person named " + rp.getWith()));
            RelationshipType relationshipType = RelationshipType.valueOf(rp.getName());
            return new C4Relationship(from, to, relationshipType);
        })).collect(Collectors.toList());
    }
}
