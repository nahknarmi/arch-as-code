package net.nahknarmi.arch.transformation.validator;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;

public interface WorkspaceValidator {
    void validate(ArchitectureDataStructure dataStructure) throws DataStructureValidationException;
}
