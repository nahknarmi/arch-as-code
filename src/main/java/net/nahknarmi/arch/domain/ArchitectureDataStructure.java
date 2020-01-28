package net.nahknarmi.arch.domain;


import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Tag;
import net.nahknarmi.arch.domain.c4.Tagable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.nahknarmi.arch.domain.c4.C4Model.NONE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArchitectureDataStructure {
    @NonNull private String name;
    @NonNull private String businessUnit;
    @NonNull private String description;
    @NonNull private List<ImportantTechnicalDecision> decisions = ImmutableList.of();
    @NonNull private C4Model model = NONE;

    public List<Tagable> getAllWithTag(C4Tag tag) {
        return Stream.of(model.getPeople(), model.getSystems(), model.getContainers(), model.getComponents())
                .flatMap(Collection::stream)
                .filter(x -> x.getTags().contains(tag))
                .collect(Collectors.toList());
    }
}
