package net.nahknarmi.arch.domain.c4;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Data
@NoArgsConstructor
public abstract class BaseEntity implements Entity {
    @NonNull
    protected C4Path path;
    @NonNull
    protected String description;
    protected List<C4Tag> tags = emptyList();
    protected List<C4Relationship> relationships = emptyList();
    protected String name;

    public BaseEntity(@NonNull C4Path path, @NonNull String description, List<C4Tag> tags, List<C4Relationship> relationships, String name) {
        this.path = path;
        this.description = description;
        this.tags = ofNullable(tags).orElse(emptyList());
        this.relationships = ofNullable(relationships).orElse(emptyList());
        this.name = name;
    }
}
