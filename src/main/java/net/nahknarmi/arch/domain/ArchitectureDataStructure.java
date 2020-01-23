package net.nahknarmi.arch.domain;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.*;

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
        Stream<C4Person> personStream = model.getPeople().stream();
        Stream<C4SoftwareSystem> systemStream = model.getSystems().stream();
        Stream<C4Container> containerStream = model.getSystems().stream().flatMap(x -> x.getContainers().stream());
        Stream<C4Component> componentStream = model.getSystems().stream().flatMap(x -> x.getContainers().stream().flatMap(c -> c.getComponents().stream()));

        return Streams.concat(personStream, systemStream, containerStream, componentStream)
                .filter(x -> x.getTags().contains(tag))
                .collect(Collectors.toList());
    }
}
