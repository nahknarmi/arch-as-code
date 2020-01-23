package net.nahknarmi.arch.domain.c4.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Tag;

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
    private List<C4Tag> tags;
    private List<C4EntityReference> entities;
}
