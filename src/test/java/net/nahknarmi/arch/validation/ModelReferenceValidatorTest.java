package net.nahknarmi.arch.validation;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class ModelReferenceValidatorTest {

    @Test
    public void validate_empty_data_structure() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }

    @Test
    public void validation_test() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        List<String> validationList = new ModelReferenceValidator().validate(dataStructure);

        assertThat(validationList, empty());
    }


}