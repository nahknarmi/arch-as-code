package net.nahknarmi.arch.domain.c4;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Relationship {
    @NonNull
    private C4Action action;
    @NonNull
    private String with;
    @NonNull
    private C4Type type;
    private C4Path path;
    private String description;
}
