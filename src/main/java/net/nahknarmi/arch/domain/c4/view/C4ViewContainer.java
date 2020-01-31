package net.nahknarmi.arch.domain.c4.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4ViewContainer {
    public static final C4ViewContainer NONE = new C4ViewContainer();

    @NonNull
    private List<C4SystemView> systemViews = emptyList();
    @NonNull
    private List<C4ContainerView> containerViews = emptyList();
    @NonNull
    private List<C4ComponentView> componentViews = emptyList();
}
