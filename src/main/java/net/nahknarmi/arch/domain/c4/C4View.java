package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4View {
    private C4SystemView systemView = C4SystemView.NONE;
    private C4ContainerView containerView = C4ContainerView.NONE;
    private C4ComponentView componentView = C4ComponentView.NONE;

    public static final C4View NONE = new C4View();
}
