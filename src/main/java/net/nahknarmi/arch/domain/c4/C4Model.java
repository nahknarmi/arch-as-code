package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Model {
    public static final C4Model NONE = new C4Model();

    @NonNull
    private List<C4Person> people = emptyList();
    @NonNull
    private List<C4SoftwareSystem> systems = emptyList();
    @NonNull
    private List<C4Container> containers = emptyList();
    @NonNull
    private List<C4Component> components = emptyList();
}
