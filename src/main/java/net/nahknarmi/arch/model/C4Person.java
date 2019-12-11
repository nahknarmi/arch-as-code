package net.nahknarmi.arch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class C4Person {
    @NonNull
    private String name;
    @NonNull
    private String description;

    C4Person() {
    }
}
