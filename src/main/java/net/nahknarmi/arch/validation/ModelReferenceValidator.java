package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.Entity;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ModelReferenceValidator implements DataStructureValidator {

    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure) {
        C4Model model = dataStructure.getModel();

        List<C4Path> allEntities = model.allEntities().stream().map(Entity::getPath).collect(toList());

        return model.allEntities()
                .stream()
                .flatMap(x -> x.getRelationships()
                        .stream()
                        .filter(z -> !allEntities.contains(z.getWith()))
                        .map(p -> "Broken relationship between " + x.getPath() + " to " + p.getWith()))
                .collect(toList());
    }
}
