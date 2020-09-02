package net.trilogy.arch.validation;

import net.trilogy.arch.domain.ArchitectureDataStructure;

import java.util.List;

import static java.util.Collections.emptyList;

public class ModelValidator implements DataStructureValidator {
    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure) {
        return emptyList();
    }
}
