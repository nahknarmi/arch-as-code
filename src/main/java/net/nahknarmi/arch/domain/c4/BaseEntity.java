package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntity implements Entity {
    @NonNull
    protected C4Path path;
    @NonNull
    protected String technology;
    @NonNull
    protected String description;
    @NonNull
    protected List<C4Tag> tags = emptyList();
    @NonNull
    protected List<C4Relationship> relationships = emptyList();
}
