package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import org.junit.Test;

import java.util.List;

import static net.trilogy.arch.validation.architectureUpdate.ValidationError.forMultipleTddContentFilesForTdd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ValidationErrorTest {

    @Test
    public void shouldProperlyFormatError_forMultipleTddContentFilesForTdd() {
        List<TddContent> tddContents = List.of(
                new TddContent("content", "TDD 1.0 : Component-1.md"),
                new TddContent("content", "TDD 1.0 : Component-1.txt")
        );

        ValidationError error = forMultipleTddContentFilesForTdd(new TddComponentReference("1"), new TddId("TDD 1.0"), tddContents);

        assertThat(error.getDescription(),
                equalTo(
                        "Component id \"1\" with TDD \"TDD 1.0\" has the following TDD content files associated with it:\n" +
                                "  - TDD 1.0 : Component-1.md\n" +
                                "  - TDD 1.0 : Component-1.txt\n"
                )
        );
    }
}
