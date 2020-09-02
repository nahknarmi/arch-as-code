package net.trilogy.arch.validation;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.Entity;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ModelReferenceValidator implements DataStructureValidator {

    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure) {
        C4Model model = dataStructure.getModel();

        List<String> allEntities = model.allEntities().stream().map(Entity::getId).collect(toList());

        return model.allEntities()
                .stream()
                .flatMap(e -> e.getRelationships()
                        .stream()
                        .filter(r -> !allEntities.contains(r.getWithId()))
                        .map(p -> "Broken relationship between " + e.getPath() + " to " + p.getWithId()))
                .collect(toList());
    }
}
