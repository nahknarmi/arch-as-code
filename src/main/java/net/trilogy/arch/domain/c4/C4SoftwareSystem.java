package net.trilogy.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static net.trilogy.arch.domain.c4.C4Location.UNSPECIFIED;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4SoftwareSystem extends Entity implements HasLocation {
    private C4Location location;

    @Builder(toBuilder = true)
    C4SoftwareSystem(String id,
                     String alias,
                     String name,
                     C4Path path,
                     String description,
                     @Singular Set<C4Tag> tags,
                     @Singular Set<C4Relationship> relationships,
                     C4Location location) {
        super(id, alias, path, name, description, tags, relationships);
        this.location = Optional.ofNullable(location).orElse(UNSPECIFIED);
    }

    public C4Type getType() {
        return C4Type.SYSTEM;
    }

    public static class C4SoftwareSystemBuilder {
        public C4SoftwareSystemBuilder path(C4Path path) {
            if (path == null) return this;
            checkArgument(C4Type.SYSTEM.equals(path.type()), format("Path %s is not valid for SoftwareSystem.", path));
            this.path = path;
            return this;
        }
    }

    @Override
    public C4SoftwareSystem shallowCopy() {
        return this.toBuilder().build();
    }
}
