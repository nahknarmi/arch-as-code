package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;

import java.util.Collections;
import java.util.List;

public class ModelReferenceValidator implements DataStructureValidator {

    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure) {

        C4Model model = dataStructure.getModel();
        model.getSystems().stream().flatMap(x -> x.getRelationships().stream());

        return Collections.emptyList();
    }
}
