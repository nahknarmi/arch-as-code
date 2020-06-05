package net.trilogy.arch.domain.c4.view;

import lombok.*;
import net.trilogy.arch.domain.c4.*;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4ComponentView extends C4View implements HasContainerReference, HasIdentity<C4Container> {
    private String containerId;
    private String containerAlias;

    @Builder
    public C4ComponentView(String key, @NonNull String name, @NonNull String description, @Singular Set<C4Tag> tags, @Singular Set<C4Reference> elements, String containerId, String containerAlias, Set<C4Reference> relationships) {
        super(key, name, description, tags, elements, relationships);
        this.containerId = containerId;
        this.containerAlias = containerAlias;
    }

    @Override
    public String getId() {
        return containerId;
    }

    @Override
    public String getAlias() {
        return containerAlias;
    }

    public C4Container getReferenced(C4Model dataStructureModel) {
        Entity result;
        if (containerId != null) {
            result = dataStructureModel.findEntityById(containerId).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + containerId));
        } else if (containerAlias != null) {
            result = dataStructureModel.findEntityByAlias(containerAlias);
        } else {
            throw new IllegalStateException("SystemView is missing id and alias: " + this);
        }

        if (result instanceof C4Container) {
            return (C4Container) result;
        } else {
            throw new IllegalStateException("ComponentView is not referencing a C4Container: " + result);
        }
    }
}
