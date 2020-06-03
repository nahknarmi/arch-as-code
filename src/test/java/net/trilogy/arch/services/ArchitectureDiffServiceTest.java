package net.trilogy.arch.services;

import net.trilogy.arch.ArchitectureDataStructureHelper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Person;
import org.junit.Test;

import java.util.Set;

import static net.trilogy.arch.ArchitectureDataStructureHelper.createPerson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureDiffServiceTest {

    @Test
    public void shouldDiffEmptyArchitectures() {
        final ArchitectureDataStructure first = ArchitectureDataStructureHelper.empty("1").build();
        final ArchitectureDataStructure second = ArchitectureDataStructureHelper.empty("2").build();

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(ArchitectureDiff.empty()));
    }

    @Test
    public void shouldDiffPeople() {
        var arch = ArchitectureDataStructureHelper.empty("1");
        final C4Person commonPerson = createPerson("2");
        final C4Person personInFirst = createPerson("1");
        final C4Person personInSecond = createPerson("3");

        var first = getArchWithPeople(arch, personInFirst, commonPerson);
        var second = getArchWithPeople(arch, personInSecond, commonPerson);

        ArchitectureDiff expected = new ArchitectureDiff(new ArchitectureDiff.PeopleDiff(
                Set.of(personInFirst),
                Set.of(personInSecond),
                Set.of(commonPerson)
        ));

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(expected));
    }

    public ArchitectureDataStructure getArchWithPeople(ArchitectureDataStructure.ArchitectureDataStructureBuilder arch, C4Person commonPerson, C4Person personInFirst) {
        return arch.model(new C4Model(
                        Set.of(personInFirst, commonPerson),
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        Set.of()
                )
        ).build();
    }
}
