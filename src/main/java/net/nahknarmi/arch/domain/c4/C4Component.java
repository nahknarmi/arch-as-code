package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Component {
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private String technology;
}