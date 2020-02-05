package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Person extends BaseEntity implements Entity, Locatable {

    public C4Person(C4Path path, String technology, String description, List<C4Tag> tags, List<C4Relationship> relationships) {
        this(path, technology, description, tags, relationships, C4Location.UNSPECIFIED);
    }

    public C4Person(C4Path path, String technology, String description, List<C4Tag> tags, List<C4Relationship> relationships, C4Location location) {
        super(path, technology, description, tags, relationships);
        this.location = location;
    }

    private C4Location location;

    public String getName() {
        return path.getPersonName();
    }
}
