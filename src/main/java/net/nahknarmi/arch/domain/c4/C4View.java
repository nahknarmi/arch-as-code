package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4View {
    private C4SystemView systemView = C4SystemView.NONE;
    private C4ContainerView containerView;
    private C4ComponentView componentView;

    public static final C4View NONE = new C4View();
}
