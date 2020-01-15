package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;

import java.util.List;

interface DataStructureValidator {
    List<String> validate(ArchitectureDataStructure dataStructure);
}
