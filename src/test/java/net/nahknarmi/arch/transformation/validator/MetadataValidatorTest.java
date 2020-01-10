package net.nahknarmi.arch.transformation.validator;

import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MetadataValidatorTest {

    public static final long ID = 99999L;

    @Test
    public void missing_name_validation() {
        ArchitectureDataStructure dataStructure =
                new ArchitectureDataStructure("", ID, "business unit", "desc", Collections.emptyList(), C4Model.NONE);

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new MetadataValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing name";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void missing_business_unit_validation() {
        ArchitectureDataStructure dataStructure =
                new ArchitectureDataStructure("name", ID, "", "desc", Collections.emptyList(), C4Model.NONE);

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new MetadataValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing business unit";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void missing_name_and_business_unit_validation() {
        ArchitectureDataStructure dataStructure =
                new ArchitectureDataStructure("", ID, "", "desc", Collections.emptyList(), C4Model.NONE);

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new MetadataValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing name\nMissing business unit";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void missing_all_validation() {
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructure();

        Exception exception = assertThrows(DataStructureValidationException.class, () -> {
            new MetadataValidator().validate(dataStructure);
        });

        String expectedMessage = "Missing name\nMissing business unit";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}
