package net.nahknarmi.arch.domain.c4.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.C4Tag;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4ComponentView {
    @NonNull
    private String name;
    @NonNull
    private C4Path containerPath;
    @NonNull
    private String description;
    private List<C4Tag> tags = emptyList();
    private List<C4Path> entities = emptyList();
}
