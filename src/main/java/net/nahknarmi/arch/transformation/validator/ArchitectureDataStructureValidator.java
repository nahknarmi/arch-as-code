package net.nahknarmi.arch.transformation.validator;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;

import java.util.List;

public interface ArchitectureDataStructureValidator {
    List<String> validate(ArchitectureDataStructure dataStructure);
}
