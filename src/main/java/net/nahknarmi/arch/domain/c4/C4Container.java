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
@AllArgsConstructor
@NoArgsConstructor
public class C4Container extends BaseEntity implements Entity, HasTechnology, HasUrl {
    @NonNull
    protected String technology;
    protected String url;

    @Builder(toBuilder = true)
    public C4Container(@NonNull C4Path path, @NonNull String description, String name, Set<C4Tag> tags, List<C4Relationship> relationships, String technology, String url) {
        super(path, description, tags, relationships, name);
        this.technology = technology;
        this.url = url;
    }

    @JsonIgnore
    public String getName() {
        return ofNullable(name)
                .orElse(path.containerName().orElseThrow(()
                        -> new IllegalStateException("Container name couldn't be extracted from " + path)));
    }

    public static class C4ContainerBuilder {
        public C4ContainerBuilder path(C4Path path) {
            checkArgument(C4Type.container.equals(path.type()), format("Path %s is not valid for Container.", path));
            this.path = path;
            return this;
        }
    }
}
