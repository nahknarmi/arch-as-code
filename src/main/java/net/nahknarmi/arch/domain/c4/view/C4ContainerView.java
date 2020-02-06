package net.nahknarmi.arch.domain.c4.view;

import lombok.*;
import net.nahknarmi.arch.domain.c4.C4Path;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4ContainerView extends C4View implements HasSystemPath {
    @NonNull
    private C4Path systemPath;

}


