package net.nahknarmi.arch.domain.c4;

import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class C4Model {
    public static final C4Model NONE = new C4Model();

    @NonNull
    @Builder.Default
    private List<C4Person> people = emptyList();
    @NonNull
    @Builder.Default
    private List<C4SoftwareSystem> systems = emptyList();
    @NonNull
    @Builder.Default
    private List<C4Container> containers = emptyList();
    @NonNull
    @Builder.Default
    private List<C4Component> components = emptyList();

    public List<Entity> allEntities() {
        return Stream.of(getSystems(), getPeople(), getComponents(), getContainers())
                .flatMap(Collection::stream).collect(toList());
    }

    public List<C4Relationship> allRelationships() {
        return allEntities().stream().flatMap(x -> x.getRelationships().stream()).collect(toList());
    }

    public Optional<Entity> getByPath(String path) {
        C4Path c4Path = new C4Path(path);

        return allEntities()
                .stream()
                .filter(x -> x.getPath().equals(c4Path))
                .findFirst();

    }
}
