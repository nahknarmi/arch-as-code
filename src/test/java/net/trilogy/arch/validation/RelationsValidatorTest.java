package net.trilogy.arch.validation;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.domain.c4.Entity;
import org.junit.Test;

import java.util.List;
import java.util.TreeSet;

import static net.trilogy.arch.ArchitectureDataStructureHelper.addSystemWithContainer;
import static net.trilogy.arch.ArchitectureDataStructureHelper.createSystemWithRelationshipsTo;
import static net.trilogy.arch.ArchitectureDataStructureHelper.softwareSystem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class RelationsValidatorTest {
    @Test
    public void validate_empty_data_structure() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        List<String> validationList = new RelationsValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }

    @Test
    public void validate_systems_with_no_relations() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();
        C4Model model = new C4Model();
        model.addSoftwareSystem(softwareSystem());
        dataStructure.setModel(model);

        List<String> validationList = new RelationsValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }

    @Test
    public void validate_components_with_no_relations() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();
        C4Model model = new C4Model();
        model = addSystemWithContainer(model, "sysId", "contId");
        dataStructure.setModel(model);

        List<String> validationList = new RelationsValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }

    @Test
    public void validate_should_pass_system_with_relation_with_tech() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();
        C4Model model = new C4Model();
        C4SoftwareSystem softwareSystem = softwareSystem();
        model.addSoftwareSystem(softwareSystem);
        TreeSet<Entity> relationsTo = new TreeSet<>();
        relationsTo.add(softwareSystem);
        model.addSoftwareSystem(createSystemWithRelationshipsTo("sys2", relationsTo));
        dataStructure.setModel(model);

        List<String> validationList = new RelationsValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }

    @Test
    public void validate_should_fail_system_with_relation_with_no_tech() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();
        C4Model model = new C4Model();
        C4SoftwareSystem softwareSystem = softwareSystem();
        model.addSoftwareSystem(softwareSystem);
        TreeSet<Entity> relationsTo = new TreeSet<>();
        relationsTo.add(softwareSystem);
        C4SoftwareSystem sys2 = createSystemWithRelationshipsTo("sys2", relationsTo);
        model.addSoftwareSystem(sys2);
        dataStructure.setModel(model);

        sys2.getRelationships().stream().findFirst().get().setTechnology("");

        List<String> validationList = new RelationsValidator().validate(dataStructure);

        assertThat(validationList, hasSize(1));

        assertThat(validationList.get(0), equalTo("Relation id sys2->1 doesn't have required technology."));
    }
}
