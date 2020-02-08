package net.nahknarmi.arch.domain.c4;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

@Data
@NoArgsConstructor
public abstract class BaseEntity implements Entity {
    @NonNull
    protected C4Path path;
    @NonNull
    protected String description;
    protected Set<C4Tag> tags = emptySet();
    protected List<C4Relationship> relationships = emptyList();
    protected String name;

    public BaseEntity(@NonNull C4Path path, @NonNull String description, Set<C4Tag> tags, List<C4Relationship> relationships, String name) {
        this.path = path;
        this.description = description;
        this.tags = ofNullable(tags).orElse(emptySet());
        this.relationships = ofNullable(relationships).orElse(emptyList());
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof C4Container)) return false;
        if (!super.equals(o)) return false;

        C4Container that = (C4Container) o;

        return getPath() != null ? getPath().equals(that.getPath()) : that.getPath() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
        return result;
    }
}
