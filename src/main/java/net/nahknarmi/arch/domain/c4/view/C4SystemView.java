package net.nahknarmi.arch.domain.c4.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Path;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4SystemView extends C4View {
    @NonNull
    private C4Path systemPath;
}
