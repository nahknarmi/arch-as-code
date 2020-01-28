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
    private C4Path with;
    private String description;
}
