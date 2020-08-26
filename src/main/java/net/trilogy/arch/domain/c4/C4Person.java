package net.trilogy.arch.domain.c4;

import lombok.*;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class C4Person extends Entity implements HasLocation {
    private C4Location location;

    public C4Type getType() {
        return C4Type.PERSON;
    }

    @Builder(toBuilder=true)
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

    public static class C4PersonBuilder {
        public C4PersonBuilder path(C4Path path) {
            if(path == null) return this;
            checkArgument(C4Type.PERSON.equals(path.type()), format("Path %s is not valid for Container.", path));
            this.path = path;
            return this;
        }
    }

    @Override
    public C4Person shallowCopy() {
        return this.toBuilder().build();
    }
}
