package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class C4Container extends BaseEntity implements Entity {

    @JsonIgnore
    public String getName() {
        return path.getContainerName().orElseThrow(() -> new IllegalStateException("Container name couldn't be extracted from " + path));
    }
}
