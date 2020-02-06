package net.nahknarmi.arch.domain.c4.view;

import lombok.*;
import net.nahknarmi.arch.domain.c4.C4Path;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4ComponentView extends C4View implements HasContainerPath {
    @NonNull
    private C4Path containerPath;
}
