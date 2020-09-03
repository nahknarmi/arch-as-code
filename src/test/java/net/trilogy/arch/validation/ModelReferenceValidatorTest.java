package net.trilogy.arch.validation;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Action;
import net.trilogy.arch.domain.c4.C4Location;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4Relationship;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptySet;
import static net.trilogy.arch.ArchitectureDataStructureHelper.softwareSystem;
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
        model.addPerson(buildPeople("200"));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));
    }

    @Test
    public void validate_person_with_system() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        C4SoftwareSystem coreBanking = softwareSystem();
        model.addSoftwareSystem(coreBanking);

        model.addPerson(buildPeople(coreBanking.getId()));
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(0));
    }

    private C4Person buildPeople(String destinationId) {
        return C4Person.builder()
                .id("2")
                .alias("@bob")
                .name("bob")
                .description("person")
                .location(C4Location.EXTERNAL)
                .relationships(of(new C4Relationship("100", null, C4Action.USES, null, destinationId, "bazz", "desc")))
                .tags(emptySet())
                .build();
    }

    @Test
    public void validate_system_with_missing_person() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        C4Model model = new C4Model();
        C4SoftwareSystem softwareSystem = softwareSystem();
        String nonExistentPersonId = "2";
        softwareSystem.setRelationships(Set.of(new C4Relationship("101", null, C4Action.DELIVERS, null, nonExistentPersonId, "batch processing", "mainframe")));
        model.addSoftwareSystem(softwareSystem);
        dataStructure.setModel(model);

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));
    }
}
