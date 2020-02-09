package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;

import java.util.List;

import static java.util.Collections.emptyList;


public class ModelValidator implements DataStructureValidator {
    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure){
        return emptyList();
    }
}
