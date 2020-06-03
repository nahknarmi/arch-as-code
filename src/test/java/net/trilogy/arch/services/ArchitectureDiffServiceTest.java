package net.trilogy.arch.services;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.*;
import org.junit.Test;

import java.util.Set;

import static net.trilogy.arch.ArchitectureDataStructureHelper.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureDiffServiceTest {

    @Test
    public void shouldDiffEmptyArchitectures() {
        final ArchitectureDataStructure first = emptyArch().build();
        final ArchitectureDataStructure second = emptyArch().build();

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(ArchitectureDiff.empty()));
    }

    @Test
    public void shouldDiffPeopleEntities() {
        var arch = emptyArch();
        final C4Person commonPerson = createPerson("2");
        final C4Person personInFirst = createPerson("1");
        final C4Person personInSecond = createPerson("3");

        var first = getArchWithPeople(arch, Set.of(personInFirst, commonPerson));
        var second = getArchWithPeople(arch, Set.of(personInSecond, commonPerson));

        ArchitectureDiff expected = new ArchitectureDiff(
                new ArchitectureDiff.PeopleDiff(
                        Set.of(personInFirst),
                        Set.of(personInSecond),
                        Set.of(commonPerson)
                ),
                ArchitectureDiff.SystemsDiff.empty()
        );

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(expected));
    }

    @Test
    public void shouldDiffPeopleRelationships() {
        var arch = emptyArch();
        final C4SoftwareSystem system2 = createSystem("2");
        final C4SoftwareSystem system3 = createSystem("3");
        final Set<C4SoftwareSystem> systems = Set.of(system2, system3);

        final C4Person personWithRelationshipsToSystem2 = createPersonWithRelationshipsTo("1", Set.of(system2));
        final C4Person personWithRelationshipsToSystem3 = createPersonWithRelationshipsTo("1", Set.of(system3));

        var first = getArch(arch, Set.of(personWithRelationshipsToSystem2), systems, Set.of(), Set.of(), Set.of());
        var second = getArch(arch, Set.of(personWithRelationshipsToSystem3), systems, Set.of(), Set.of(), Set.of());

        ArchitectureDiff expected = new ArchitectureDiff(
                new ArchitectureDiff.PeopleDiff(
                        Set.of(personWithRelationshipsToSystem2),
                        Set.of(personWithRelationshipsToSystem3),
                        Set.of()
                ),
                new ArchitectureDiff.SystemsDiff(
                        Set.of(),
                        Set.of(),
                        systems
                )
        );

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(expected));
    }

    @Test
    public void shouldDiffSystemsEntities() {
        var arch = emptyArch();
        C4SoftwareSystem commonSystem = createSystem("2");
        C4SoftwareSystem systemInFirst = createSystem("1");
        C4SoftwareSystem systemInSecond = createSystem("3");

        var first = getArchWithSystems(arch, Set.of(systemInFirst, commonSystem));
        var second = getArchWithSystems(arch, Set.of(systemInSecond, commonSystem));

        ArchitectureDiff expected = new ArchitectureDiff(
                ArchitectureDiff.PeopleDiff.empty(),
                new ArchitectureDiff.SystemsDiff(
                        Set.of(systemInFirst),
                        Set.of(systemInSecond),
                        Set.of(commonSystem)
                ));

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(expected));
    }


    @Test
    public void shouldDiffSystemRelationships() {
        var arch = emptyArch();
        final C4SoftwareSystem system2 = createSystem("2");
        final C4SoftwareSystem system3 = createSystem("3");
        final Set<C4SoftwareSystem> commonSystems = Set.of(system2, system3);

        C4SoftwareSystem systemWithRelationshipToSystem2 = createSystemWithRelationshipsTo("1", Set.of(system2));
        C4SoftwareSystem systemWithRelationshipToSystem3 = createSystemWithRelationshipsTo("1", Set.of(system3));

        var first = getArchWithSystems(arch, Set.of(systemWithRelationshipToSystem2, system2, system3));
        var second = getArchWithSystems(arch, Set.of(systemWithRelationshipToSystem3, system2, system3));

        ArchitectureDiff expected = new ArchitectureDiff(
                new ArchitectureDiff.PeopleDiff(
                        Set.of(),
                        Set.of(),
                        Set.of()
                ),
                new ArchitectureDiff.SystemsDiff(
                        Set.of(systemWithRelationshipToSystem2),
                        Set.of(systemWithRelationshipToSystem3),
                        commonSystems
                )
        );

        assertThat(ArchitectureDiffService.diff(first, second), equalTo(expected));
    }

    private ArchitectureDataStructure getArchWithPeople(ArchitectureDataStructure.ArchitectureDataStructureBuilder arch, Set<C4Person> people) {
        return arch.model(
                new C4Model(people, Set.of(), Set.of(), Set.of(), Set.of()
                )
        ).build();
    }

    private ArchitectureDataStructure getArchWithSystems(ArchitectureDataStructure.ArchitectureDataStructureBuilder arch, Set<C4SoftwareSystem> systems) {
        return arch.model(
                new C4Model(Set.of(), systems, Set.of(), Set.of(), Set.of()
                )
        ).build();
    }

    private ArchitectureDataStructure getArch(ArchitectureDataStructure.ArchitectureDataStructureBuilder arch,
                                              Set<C4Person> people,
                                              Set<C4SoftwareSystem> systems,
                                              Set<C4Container> containers,
                                              Set<C4Component> components,
                                              Set<C4DeploymentNode> deploymentNodes) {
        return arch.model(
                new C4Model(people, systems, containers, components, deploymentNodes)
        ).build();
    }
}
