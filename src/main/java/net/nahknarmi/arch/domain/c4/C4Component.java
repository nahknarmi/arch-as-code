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
public class C4Component extends BaseEntity implements Entity, HasTechnology, HasUrl {
    @NonNull
    protected String technology;
    protected String url;

    @Builder(toBuilder = true)
    public C4Component(String name, @NonNull C4Path path, @NonNull String description, Set<C4Tag> tags, List<C4Relationship> relationships, String technology, String url) {
        super(path, description, tags, relationships, name);
        this.technology = technology;
        this.url = url;
    }

    @JsonIgnore
    public String getName() {
        return ofNullable(this.name).orElse(path.componentName().orElseThrow(() -> new IllegalStateException("Component name could not be derived.")));
    }

    public static class C4ComponentBuilder {
        public C4ComponentBuilder path(C4Path path) {
            checkArgument(C4Type.component.equals(path.type()), format("Path %s is not valid for Component.", path));
            this.path = path;
            return this;
        }
    }
}
