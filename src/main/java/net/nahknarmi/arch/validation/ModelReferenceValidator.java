package net.nahknarmi.arch.validation;

import com.google.common.collect.Streams;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.RelationshipPair;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelReferenceValidator implements DataStructureValidator {

    @Override
    public List<String> validate(ArchitectureDataStructure dataStructure) {

        C4Model model = dataStructure.getModel();
        Stream<RelationshipPair> systemRelationships = model.getSystems().stream().flatMap(x -> x.getRelationships().stream());
        Stream<RelationshipPair> personRelationships = model.getPersons().stream().flatMap(x -> x.getRelationships().stream());
        Stream<RelationshipPair> allRelationships = Streams.concat(systemRelationships, personRelationships);

        Stream<String> systemNames = model.getSystems().stream().map(C4SoftwareSystem::getName);
        Stream<String> personNames = model.getPersons().stream().map(C4Person::getName);
        List<String> allEntities = Streams.concat(systemNames, personNames).collect(toList());

        return allRelationships
                .filter(x -> !allEntities.contains(x.getWith()))
                .map(x -> "Broken relationship " + x)
                .collect(toList());
    }
}
