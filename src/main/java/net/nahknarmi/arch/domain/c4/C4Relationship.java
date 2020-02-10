package net.nahknarmi.arch.domain.c4;


import lombok.*;

@Data
@NoArgsConstructor
public class C4Relationship {
    @NonNull
    private C4Action action;
    @NonNull
    private C4Path with;
    @NonNull
    private String description;
    private String technology;

    @Builder
    public C4Relationship(@NonNull C4Action action, @NonNull C4Path with, @NonNull String description, String technology) {
        this.action = action;
        this.with = with;
        this.description = description;
        this.technology = technology;
    }
}
