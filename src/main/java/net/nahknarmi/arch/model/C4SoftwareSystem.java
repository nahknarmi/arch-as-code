package net.nahknarmi.arch.model;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class C4SoftwareSystem implements Relatable {
    @NonNull
    private String name;
    @NonNull
    private String description;

    C4SoftwareSystem() {
    }

    @Override
    public List<Relatable> relations() {
        return ImmutableList.of();
    }
}
