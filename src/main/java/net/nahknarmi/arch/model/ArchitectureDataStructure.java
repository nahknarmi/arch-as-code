package net.nahknarmi.arch.model;


import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class ArchitectureDataStructure {
    @NonNull private String name;
    @NonNull private Long id;
    @NonNull private String businessUnit;
    @NonNull private String description;
    @NonNull private List<ImportantTechnicalDecision> decisions = ImmutableList.of();
    @NonNull private C4Model model;

    ArchitectureDataStructure() {
    }
}
