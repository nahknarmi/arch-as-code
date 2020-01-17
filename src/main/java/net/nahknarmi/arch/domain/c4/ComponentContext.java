package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentContext {
    @NonNull
    private String name;
    @NonNull
    private String container;
    @NonNull
    private String description;
    @NonNull
    private List<C4Entity> entities;
}
