package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.RelationshipPair;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
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
        model.setPersons(of(new C4Person("bob", "person", of(new RelationshipPair("foo", "uses", "bazz", "desc")))));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));
    }

    @Test
    public void validate_person_with_system() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        model.setSystems(of(new C4SoftwareSystem("OBP", "core banking", of(), of())));

        model.setPersons(of(new C4Person("bob", "person", of(new RelationshipPair("Logs into", "uses", "OBP", "desc")))));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(0));
    }

    @Test
    public void validate_system_with_missing_person() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        model.setSystems(of(softwareSystem()));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));
    }

    private C4SoftwareSystem softwareSystem() {
        return new C4SoftwareSystem("OBP", "core banking", of(), of(new RelationshipPair("OBP", "uses", "bazz", "desc")));
    }


}