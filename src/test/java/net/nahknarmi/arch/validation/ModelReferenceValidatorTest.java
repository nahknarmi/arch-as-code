package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

public class ModelReferenceValidatorTest {

    @Test
    public void validate_empty_data_structure() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }

    @Test
    public void validate_person_with_missing_system() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        model.setPeople(of(buildPeople(new C4Path("c4://acme/spa"))));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));
    }

    @Test
    public void validate_person_with_system() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        C4SoftwareSystem coreBanking = softwareSystem();
        model.setSystems(of(coreBanking));

        model.setPeople(of(buildPeople(coreBanking.getPath())));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(0));
    }

    private C4Person buildPeople(C4Path relationshipWith) {
        return new C4Person(new C4Path("@bob"), "person", C4Location.EXTERNAL, emptyList(), of(new C4Relationship(C4Action.DELIVERS, relationshipWith, "bazz", "desc")));
    }

    @Test
    public void validate_system_with_missing_person() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        C4SoftwareSystem softwareSystem = softwareSystem();
        softwareSystem.setRelationships(of(new C4Relationship(C4Action.DELIVERS, new C4Path("@bob"), "batch processing", "mainframe")));
        model.setSystems(of(softwareSystem));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));
    }

    private C4SoftwareSystem softwareSystem() {
        return new C4SoftwareSystem(new C4Path("c4://OBP"), "core banking", C4Location.EXTERNAL, of(), of());
    }

}