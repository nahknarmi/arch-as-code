package net.trilogy.arch.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.*;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Tag;
import net.trilogy.arch.domain.c4.Entity;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ArchitectureDataStructure {
    @NonNull
    private String name;
    @NonNull
    private String businessUnit;
    @NonNull
    private String description;
    @NonNull
    @Builder.Default
    private List<ImportantTechnicalDecision> decisions = ImmutableList.of();
    @NonNull
    @Builder.Default
    @JsonIgnore
    private List<DocumentationSection> documentation = ImmutableList.of();
    @NonNull
    @Builder.Default
    @JsonIgnore
    private List<DocumentationImage> documentationImages = ImmutableList.of();
    @NonNull
    @Builder.Default
    private C4Model model = C4Model.NONE;

    public Set<Entity> getAllWithTag(C4Tag tag) {
        return model.findWithTag(tag);
    }
}
