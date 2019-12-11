package net.nahknarmi.arch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
public class C4Model {
    @NonNull
    private List<C4Person> persons = emptyList();
    @NonNull
    private List<C4SoftwareSystem> systems = emptyList();

    C4Model() {
    }


}
