package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;

import java.util.ArrayList;
import java.util.List;


public class ModelValidator implements DataStructureValidator {
    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure){
        C4Model model = dataStructure.getModel();
        List<String> errors = new ArrayList<>();


        if (model.getSystems().isEmpty()) {
            errors.add("Missing at least one system");
        }

        if (model.getPeople().isEmpty()) {
            errors.add("Missing at least one person");
        }

        return errors;
    }
}
