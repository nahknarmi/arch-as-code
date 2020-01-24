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
public class C4Person implements Entity {
    @NonNull
    private C4Path path;
    @NonNull
    private String name;
    @NonNull
    private String description;

    private List<C4Tag> tags = emptyList();
    private List<C4Relationship> relationships = emptyList();
}
