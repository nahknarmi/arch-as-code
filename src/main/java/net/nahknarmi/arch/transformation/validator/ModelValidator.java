package net.nahknarmi.arch.transformation.validator;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;

import java.util.ArrayList;
import java.util.List;


public class ModelValidator implements WorkspaceValidator {
    @Override
    public void validate(ArchitectureDataStructure dataStructure) throws DataStructureValidationException {
        C4Model model = dataStructure.getModel();

        if (model == C4Model.NONE) {
            throw new DataStructureValidationException("Missing model");
        }

        List<String> errors = new ArrayList<>();


        if (model.getSystems() == null || model.getSystems().size() == 0) {
            errors.add("Missing at least one system");
        }

        if (model.getPersons() == null || model.getPersons().size() == 0) {
            errors.add("Missing at least one person");
        }

        if (!errors.isEmpty()) {
            String message = String.join("\n", errors);

            throw new DataStructureValidationException(message);
        }
    }
}
