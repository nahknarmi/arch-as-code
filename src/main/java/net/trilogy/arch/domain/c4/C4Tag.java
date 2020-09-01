package net.trilogy.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class C4Tag {
    @NonNull
    String tag;

    @JsonValue
    public String getTag() {
        return tag;
    }
}
