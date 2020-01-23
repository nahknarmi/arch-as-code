package net.nahknarmi.arch.domain.c4.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.C4Type;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4EntityReference {
    @NonNull
    private String name;
    @NonNull
    private C4Type type;
    private C4Path path;
}
