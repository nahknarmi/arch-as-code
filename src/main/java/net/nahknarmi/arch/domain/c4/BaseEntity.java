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
abstract class BaseEntity implements Entity {
    @NonNull
    protected C4Path path;
    @NonNull
    private String technology;
    @NonNull
    private String description;
    @NonNull
    private List<C4Tag> tags = emptyList();
    @NonNull
    private List<C4Relationship> relationships = emptyList();
}
