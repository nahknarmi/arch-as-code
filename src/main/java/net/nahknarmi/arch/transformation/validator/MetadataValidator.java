package net.nahknarmi.arch.transformation.validator;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;

import java.util.ArrayList;
import java.util.List;


public class MetadataValidator implements WorkspaceValidator {
    @Override
    public void validate(ArchitectureDataStructure dataStructure) throws DataStructureValidationException {
        List<String> errors = new ArrayList<String>();

        if (dataStructure.getName() == null || dataStructure.getName().isEmpty()) {
            errors.add("Missing name");
        }

        if (dataStructure.getBusinessUnit() == null || dataStructure.getBusinessUnit().isEmpty()) {
            errors.add("Missing business unit");
        }

        if (!errors.isEmpty()) {
            String message = String.join("\n", errors);

            throw new DataStructureValidationException(message);
        }
    }
}
