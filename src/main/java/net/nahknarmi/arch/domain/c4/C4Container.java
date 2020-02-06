package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class C4Container extends BaseEntity implements Entity, HasTechnology, HasUrl {
    @NonNull
    protected String technology;
    protected String url;

    C4Container() {
        super();
    }

    @Builder
    C4Container(@NonNull C4Path path, @NonNull String technology, @NonNull String description, @NonNull List<C4Tag> tags, @NonNull List<C4Relationship> relationships, String url) {
        super(path, description, tags, relationships);
        this.technology = technology;
        this.url = url;
    }

    @JsonIgnore
    public String getName() {
        return path.getContainerName().orElseThrow(() -> new IllegalStateException("Container name couldn't be extracted from " + path));
    }
}
