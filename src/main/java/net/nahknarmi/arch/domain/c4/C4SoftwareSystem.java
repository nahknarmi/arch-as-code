package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static net.nahknarmi.arch.domain.c4.C4Location.UNSPECIFIED;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4SoftwareSystem extends BaseEntity implements Entity, Locatable {
    private C4Location location;

    @Builder
    C4SoftwareSystem(String name, @NonNull C4Path path, @NonNull String description, Set<C4Tag> tags, List<C4Relationship> relationships, C4Location location) {
        super(path, description, tags, relationships, name);
        this.location = Optional.ofNullable(location).orElse(UNSPECIFIED);
    }

    @JsonIgnore
    public String getName() {
        return ofNullable(name).orElse(path.getSystemName());
    }
}
