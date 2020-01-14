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
public class C4SystemView {
    @NonNull
    private List<SystemContext> systems = emptyList();

    public static final C4SystemView NONE = new C4SystemView();
}
