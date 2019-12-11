package net.nahknarmi.arch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return fromRelationships(Stream.concat(systems.stream(), this.persons.stream()));
    }

    private List<C4Relationship> fromRelationships(@NonNull Stream<Relatable> stream) {
        return stream.flatMap(from -> from.relations().stream().map(rp -> {
            Relatable to = Stream.concat(systems.stream(), this.persons.stream())
                    .filter(x -> x.getName().equals(rp.getWith()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to find system or person named " + rp.getWith()));
            RelationshipType relationshipType = RelationshipType.valueOf(rp.getName());
            return new C4Relationship(from, to, relationshipType);
        })).collect(Collectors.toList());
    }
}
