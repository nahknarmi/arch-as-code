package net.nahknarmi.arch.transformation.validator;

import net.nahknarmi.arch.TestHelper;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.validation.ModelValidator;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static net.nahknarmi.arch.TestHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelDataStructureValidatorTest {

    @Test
    public void missing_system_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(TestHelper.noSystemModel());

        List<String> validationMessages = new ModelValidator().validate(dataStructure);

        assertThat(validationMessages, Matchers.containsInAnyOrder("Missing at least one system"));
    }

    @Test
    public void missing_person_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(noPersonModel());

        List<String> validationMessages = new ModelValidator().validate(dataStructure);

        assertThat(validationMessages, Matchers.containsInAnyOrder("Missing at least one person"));
    }

    @Test
    public void missing_system_and_person_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(noSystemNoPersonModel());

        List<String> validationMessages = new ModelValidator().validate(dataStructure);

        assertThat(validationMessages, Matchers.containsInAnyOrder(
                "Missing at least one system",
                "Missing at least one person"));
    }

}
