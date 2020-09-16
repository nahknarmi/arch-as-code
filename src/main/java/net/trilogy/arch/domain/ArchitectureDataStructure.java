package net.trilogy.arch.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.trilogy.arch.domain.c4.C4Model;

import java.util.List;

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
    private C4Model model = C4Model.EMPTY;
}
