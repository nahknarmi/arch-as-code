package net.nahknarmi.arch.domain.c4;

import lombok.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class C4Model {
    public static final C4Model NONE = new C4Model();

    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private Set<C4Person> people = new HashSet<>();
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private Set<C4SoftwareSystem> systems = new HashSet<>();
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private Set<C4Container> containers = new HashSet<>();
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    private Set<C4Component> components = new HashSet<>();

    public C4Model addPerson(C4Person person) {
        checkArgument(!personWithNameExists(person), format("Person with name '%s' already exists.", person.name()));
        checkArgument(!entityWithPathExists(person, people), format("Person with path '%s' already exists.", person));

        people.add(person);

        return this;
    }

    public C4Model addSoftwareSystem(C4SoftwareSystem softwareSystem) {
        checkArgument(!systemWithNameExists(softwareSystem), format("Software System with name '%s' already exists.", softwareSystem.name()));
        checkArgument(!entityWithPathExists(softwareSystem, systems), format("Software System given path '%s' already exists.", softwareSystem));

        systems.add(softwareSystem);

        return this;
    }

    public C4Model addContainer(C4Container container) {
        checkArgument(systemPathExists(container.getPath()), format("System for container (%s) doesn't exist in model.", container.getPath()));

        containers.add(container);

        return this;
    }

    public C4Model addComponent(C4Component component) {
        C4Path path = component.getPath();
        checkArgument(systemPathExists(path) && containerPathExists(path), format("System or Container for component (%s) doesn't exist in model.", path));

        components.add(component);

        return this;
    }

    public Set<Entity> allEntities() {
        return Stream.of(getSystems(), getPeople(), getComponents(), getContainers())
                .flatMap(Collection::stream).collect(toSet());
    }

    public List<C4Relationship> allRelationships() {
        return allEntities().stream().flatMap(x -> x.getRelationships().stream()).collect(toList());
    }

    public C4Person findPersonByName(String name) {
        checkNotNull(name);
        return getPeople()
                .stream()
                .filter(x -> x.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find person with name - " + name));
    }

    public Entity findByPath(C4Path path) {
        checkNotNull(path);
        return allEntities()
                .stream()
                .filter(x -> x.getPath().equals(path))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find entity with path " + path));
    }

    public Set<Entity> findWithTag(C4Tag tag) {
        checkNotNull(tag);
        return allEntities()
                .stream()
                .filter(x -> x.getTags().contains(tag))
                .collect(toSet());
    }

    private <T extends Entity> boolean entityWithPathExists(T entity, Set<T> set) {
        return set.stream().anyMatch(s -> s.getPath().equals(entity.getPath()));
    }

    private boolean systemPathExists(C4Path path) {
        return getSystems().stream().map(BaseEntity::getPath).anyMatch(p -> p.equals(path.systemPath()));
    }

    private boolean containerPathExists(C4Path path) {
        return getContainers().stream().map(BaseEntity::getPath).anyMatch(p -> p.equals(path.containerPath()));
    }

    private boolean personWithNameExists(C4Person person) {
        return getPeople().stream().anyMatch(x -> x.name().equals(person.name()));
    }

    private boolean systemWithNameExists(C4SoftwareSystem system) {
        return getSystems().stream().anyMatch(x -> x.name().equals(system.name()));
    }
}
