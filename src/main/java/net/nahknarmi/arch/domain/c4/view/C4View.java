package net.nahknarmi.arch.domain.c4.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.C4Tag;

import java.util.List;

import static java.util.Collections.emptyList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class C4View {
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private List<C4Tag> tags = emptyList();
    @JsonProperty("references")
    private List<C4Path> entities = emptyList();
}
