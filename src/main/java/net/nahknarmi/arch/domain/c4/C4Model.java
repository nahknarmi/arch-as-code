package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Model {
    public static final C4Model NONE = new C4Model();

    @NonNull
    private List<C4Person> people = emptyList();
    @NonNull
    private List<C4SoftwareSystem> systems = emptyList();
    @NonNull
    private List<C4Container> containers = emptyList();
    @NonNull
    private List<C4Component> components = emptyList();

    public List<Entity> allEntities() {
        return Stream.of(getSystems(), getPeople(), getComponents(), getContainers())
                .flatMap(Collection::stream).collect(toList());
    }

    public List<C4Relationship> allRelationships() {
        return allEntities().stream().flatMap(x -> x.getRelationships().stream()).collect(toList());
    }
}
