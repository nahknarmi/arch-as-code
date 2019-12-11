package net.nahknarmi.arch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class C4Model {
    @NonNull
    private C4Person person;

    C4Model() {
    }
}
