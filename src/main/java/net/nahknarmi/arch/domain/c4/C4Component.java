package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class C4Component extends BaseEntity implements Entity, HasTechnology, HasUrl {
    @NonNull
    protected String technology;
    protected String url;

    C4Component() {
        super();
    }

    @Builder
    C4Component(@NonNull C4Path path, @NonNull String technology, @NonNull String description, @NonNull List<C4Tag> tags, @NonNull List<C4Relationship> relationships, String url) {
        super(path, description, tags, relationships);
        this.technology = technology;
        this.url = url;
    }

    @JsonIgnore
    public String getName() {
        return path.getComponentName().orElseThrow(() -> new IllegalStateException("Workspace Id not found!!"));
    }
}
