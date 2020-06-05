package net.trilogy.arch.domain.c4.view;

import lombok.*;
import net.trilogy.arch.domain.c4.*;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4SystemView extends C4View implements HasSystemReference, HasIdentity<C4SoftwareSystem> {
    private String systemId;
    private String systemAlias;

    @Builder
    public C4SystemView(String key, @NonNull String name, @NonNull String description, @Singular Set<C4Tag> tags, @Singular Set<C4Reference> elements, String systemId, String systemAlias, Set<C4Reference> relationships) {
        super(key, name, description, tags, elements, relationships);
        this.systemId = systemId;
        this.systemAlias = systemAlias;
    }

    @Override
    public String getId() {
        return systemId;
    }

    @Override
    public String getAlias() {
        return systemAlias;
    }

    public C4SoftwareSystem getReferenced(C4Model dataStructureModel) {
        Entity result;
        if (systemId != null) {
            result = dataStructureModel.findEntityById(systemId).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + systemId));
        } else if (systemAlias != null) {
            result = dataStructureModel.findEntityByAlias(systemAlias);
        } else {
            throw new IllegalStateException("SystemView is missing id and alias: " + this);
        }

        if (result instanceof C4SoftwareSystem) {
            return (C4SoftwareSystem) result;
        } else {
            throw new IllegalStateException("SystemView is not referencing a C4SoftwareSystem: " + result);
        }
    }
}
