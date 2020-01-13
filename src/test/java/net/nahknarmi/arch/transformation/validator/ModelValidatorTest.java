package net.nahknarmi.arch.transformation.validator;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.C4View;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ModelValidatorTest {

    @Test
    public void missing_model_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(C4Model.NONE);

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new ModelValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing model";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void missing_system_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(noSystemModel());

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new ModelValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing at least one system";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void missing_person_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(noPersonModel());

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new ModelValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing at least one person";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void missing_system_and_person_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(noSystemNoPersonModel());

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new ModelValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing at least one system\nMissing at least one person";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    private ArchitectureDataStructure getDataStructure(C4Model model) {
        return new ArchitectureDataStructure("name", "business unit", "desc", Collections.emptyList(), model);
    }

    private C4Model noPersonModel() {
        return new C4Model(Collections.emptyList(), ImmutableList.of(new C4SoftwareSystem()), new C4View());
    }

    private C4Model noSystemModel() {
        return new C4Model(ImmutableList.of(new C4Person()), Collections.emptyList(), new C4View());
    }

    private C4Model noSystemNoPersonModel() {
        return new C4Model(Collections.emptyList(), Collections.emptyList(), new C4View());
    }
}
