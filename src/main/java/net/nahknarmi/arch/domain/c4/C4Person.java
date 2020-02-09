package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class C4Person extends BaseEntity implements Entity, HasLocation {
    private C4Location location;

    @Builder
    C4Person(String name, @NonNull C4Path path, @NonNull String description, Set<C4Tag> tags, List<C4Relationship> relationships, C4Location location) {
        super(path, description, tags, relationships, name);
        this.location = location;
    }

    @JsonIgnore
    public String getName() {
        return ofNullable(this.name).orElse(path.personName());
    }

    public static class C4PersonBuilder {

        public C4PersonBuilder path(C4Path path) {
            checkArgument(C4Type.person.equals(path.type()), format("Path %s is not valid for Container.", path));
            this.path = path;
            return this;
        }
    }
}
