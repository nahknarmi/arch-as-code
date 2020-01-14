package net.nahknarmi.arch.transformation.validator;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.C4View;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class ModelValidatorTest {

    @Test
    public void missing_system_validation() {
        ArchitectureDataStructure dataStructure = getDataStructure(noSystemModel());

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
