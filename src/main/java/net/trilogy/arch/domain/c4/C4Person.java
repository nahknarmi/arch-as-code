package net.trilogy.arch.domain.c4;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class C4Person extends Entity implements HasLocation {
    private C4Location location;

    @Builder(toBuilder = true)
    C4Person(String id,
             String alias,
             String name,
             C4Path path,
             String description,
             @Singular Set<C4Tag> tags,
             @Singular Set<C4Relationship> relationships,
             C4Location location) {
        super(id, alias, path, name, description, tags, relationships);
        this.location = location;
    }

    @Override
    public C4Type getType() {
        return C4Type.PERSON;
    }

    @Override
    public C4Person shallowCopy() {
        return toBuilder().build();
    }

    public static class C4PersonBuilder {
        public C4PersonBuilder path(C4Path path) {
            if (path == null) return this;
            checkArgument(C4Type.PERSON.equals(path.type()), format("Path %s is not valid for Container.", path));
            this.path = path;
            return this;
        }
    }
}
