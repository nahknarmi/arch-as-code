package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

import static java.util.Optional.ofNullable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class C4Person extends BaseEntity implements Entity, Locatable {
    private C4Location location;

    @Builder
    C4Person(String name, @NonNull C4Path path, @NonNull String description, List<C4Tag> tags, List<C4Relationship> relationships, C4Location location) {
        super(path, description, tags, relationships, name);
        this.location = location;
    }

    @JsonIgnore
    public String getName() {
        return ofNullable(this.name).orElse(path.getPersonName());
    }
}
