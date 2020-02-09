package net.nahknarmi.arch.domain.c4;

import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class C4Model {
    public static final C4Model NONE = new C4Model();

    @NonNull
    @Builder.Default
    private Set<C4Person> people = emptySet();
    @NonNull
    @Builder.Default
    private Set<C4SoftwareSystem> systems = emptySet();
    @NonNull
    @Builder.Default
    private Set<C4Container> containers = emptySet();
    @NonNull
    @Builder.Default
    private Set<C4Component> components = emptySet();

    public Set<Entity> allEntities() {
        return Stream.of(getSystems(), getPeople(), getComponents(), getContainers())
                .flatMap(Collection::stream).collect(toSet());
    }

    public List<C4Relationship> allRelationships() {
        return allEntities().stream().flatMap(x -> x.getRelationships().stream()).collect(toList());
    }

    public Optional<C4Person> personByName(String name) {
        checkNotNull(name);
        return getPeople().stream().filter(x -> x.getName().equals(name)).findFirst();
    }

    public Optional<Entity> findByPath(C4Path path) {
        return findByPath(path.getPath());
    }

    public Optional<Entity> findByPath(String path) {
        return allEntities()
                .stream()
                .filter(x -> x.getPath().equals(new C4Path(path)))
                .findFirst();
    }
}
