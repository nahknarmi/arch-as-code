package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.BaseEntity;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ModelValidator implements DataStructureValidator {
    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure){
        C4Model model = dataStructure.getModel();
        List<String> errors = new ArrayList<>();

        List<C4Path> collect = model.allEntities().stream().map(Entity::getPath).distinct().collect(Collectors.toList());

        if (collect.size() != model.allEntities().size()) {
            System.err.println("Here");
        }

        model.getComponents()
                .stream()
                .map(BaseEntity::getPath)
                .sorted((x, y) -> x.getPath().compareTo(y.getPath()))
                .forEach(x -> System.err.println(x.getPath()));

        return errors;
    }
}
