package net.trilogy.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public abstract class Entity implements HasRelation, HasTag, HasIdentity, Comparable<Entity> {
    @NonNull
    protected String id;
    protected String alias;
    @JsonUnwrapped
    protected C4Path path;
    @NonNull
    protected String name;
    protected String description;
    protected Set<C4Tag> tags = emptySet();
    protected Set<C4Relationship> relationships = emptySet();

    public Entity(@NonNull String id, String alias, C4Path path, @NonNull String name, String description, Set<C4Tag> tags, Set<C4Relationship> relationships) {
        this.id = id;
        this.alias = alias;
        this.path = path;
        this.description = description;
        this.tags = ofNullable(tags).orElse(emptySet());
        this.relationships = ofNullable(relationships).orElse(emptySet());
        this.name = name;
    }

    public abstract Entity shallowCopy();

    public abstract C4Type getType();

    @Override
    public int compareTo(Entity other) {
        return this.getId().compareTo(other.getId());
    }

    public void setPath(String path) {
        if (path == null) {
            this.path = null;
            return;
        }

        this.path = new C4Path(path);
    }

    public void setPath(C4Path path) {
        if (path == null) {
            this.path = null;
            return;
        }

        this.path = path;
    }
}
