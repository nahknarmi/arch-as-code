package net.trilogy.arch.domain;

import net.trilogy.arch.ArchitectureDataStructureHelper;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffSet;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.domain.diff.DiffableRelationship;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DiffSetTest {
    @Test
    public void systemLevelDiffsShouldHaveSystemsAndPeople() {
        var diffset = getDiffSetWithAllTypesOfDiffs();
        var actual = diffset.getSystemLevelDiffs();
        assertThat(actual, equalTo(Set.of(getPersonDiff(), getSystemDiff())));
    }

    @Test
    public void systemLevelDiffsShouldHaveTheirRelationships() {
        var relWithSource = getRelationshipDiff("1", getId(getSystemDiff()), "nonexistant-id");
        var relWithDestination = getRelationshipDiff("2", "nonexistant-id", getId(getPersonDiff()));
        var relWithBoth = getRelationshipDiff("3", getId(getPersonDiff()), getId(getSystemDiff()));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(relWithSource, relWithDestination, relWithBoth);
        var actual = diffset.getSystemLevelDiffs();

        assertThat(actual, equalTo(Set.of(getPersonDiff(), getSystemDiff(), relWithBoth)));
    }

    @Test
    public void containerLevelDiffsShouldHaveContainersOwnedBySystem() {
        var containerOwnedBySystem = getContainerDiff("container-id", getId(getSystemDiff()));
        var componentOwnedBySystem = getComponentDiff("component-id", getId(containerOwnedBySystem));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(containerOwnedBySystem, componentOwnedBySystem);
        var actual = diffset.getContainerLevelDiffs(getSystemDiff().getElement().getId());

        assertThat(actual, equalTo(Set.of(containerOwnedBySystem)));
    }

    @Test
    public void containerLevelDiffsShouldHaveTheirRelationships() {
        var container1OwnedBySystem = getContainerDiff("container-id-2", getId(getSystemDiff()));
        var container2OwnedBySystem = getContainerDiff("container-id-1", getId(getSystemDiff()));

        final Diff externalContainer = getContainerDiff();
        var relWithSource = getRelationshipDiff("1", getId(container1OwnedBySystem), getId(externalContainer));
        var relWithDestination = getRelationshipDiff("2", getId(externalContainer), getId(container1OwnedBySystem));
        var relWithBoth = getRelationshipDiff("3", getId(container1OwnedBySystem), getId(container2OwnedBySystem));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(
                container1OwnedBySystem,
                container2OwnedBySystem,
                relWithBoth,
                relWithDestination,
                relWithSource
        );
        var actual = diffset.getContainerLevelDiffs(getSystemDiff().getElement().getId());

        assertThat(actual, equalTo(Set.of(container1OwnedBySystem, container2OwnedBySystem, externalContainer, relWithBoth, relWithSource, relWithDestination)));
    }

    @Test
    public void containerLevelDiffsShouldNotHaveComponentsAndTheirRelationships() {
        var container1OwnedBySystem = getContainerDiff("container-id-2", getId(getSystemDiff()));
        var container2OwnedBySystem = getContainerDiff("container-id-1", getId(getSystemDiff()));

        var relWithSource = getRelationshipDiff("1", getId(container1OwnedBySystem), getId(getComponentDiff()));
        var relWithDestination = getRelationshipDiff("2", getId(getComponentDiff()), getId(container1OwnedBySystem));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(
                container1OwnedBySystem,
                container2OwnedBySystem,
                relWithDestination,
                relWithSource
        );
        var actual = diffset.getContainerLevelDiffs(getSystemDiff().getElement().getId());

        assertThat(actual, equalTo(Set.of(container1OwnedBySystem, container2OwnedBySystem)));
    }

    @Test
    public void containerLevelDiffsShouldHaveExternalEntities() {
        // GIVEN
        var container = getContainerDiff("container", getId(getSystemDiff()));
        var personOutsideSystem = getPersonDiff("other-person");
        var outsideSystem = getSystemDiff("other-system");
        var containerOutsideSystem = getContainerDiff("other-container", "other-system");

        var relWithSystem = getRelationshipDiff("1", getId(container), getId(outsideSystem));
        var relWithPerson = getRelationshipDiff("2", getId(personOutsideSystem), getId(container));
        var relWithContainer = getRelationshipDiff("3", getId(containerOutsideSystem), getId(container));

        // WHEN
        var diffset = getDiffSetWithAllTypesOfDiffsPlus(
                container,
                personOutsideSystem,
                outsideSystem,
                containerOutsideSystem,
                relWithSystem,
                relWithPerson,
                relWithContainer
        );
        var actual = diffset.getContainerLevelDiffs(getSystemDiff().getElement().getId());

        // THEN
        assertThat(actual, equalTo(
                Set.of(container,
                        personOutsideSystem,
                        outsideSystem,
                        containerOutsideSystem,
                        relWithSystem,
                        relWithPerson,
                        relWithContainer)
        ));
    }

    @Test
    public void componentLevelDiffsShouldHaveComponentsOwnedByContainer() {
        var componentOwnedByContainer = getComponentDiff("component-id", getId(getContainerDiff()));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(componentOwnedByContainer);
        var actual = diffset.getComponentLevelDiffs(getContainerDiff().getElement().getId());

        assertThat(actual, equalTo(Set.of(componentOwnedByContainer)));
    }

    @Test
    public void componentLevelDiffsShouldHaveTheirRelationships() {
        var component1OwnedByContainer = getComponentDiff("component-id-2", getId(getContainerDiff()));
        var component2OwnedByContainer = getComponentDiff("component-id-1", getId(getContainerDiff()));

        var relWithSource = getRelationshipDiff("1", getId(component1OwnedByContainer), "nonexistant-id");
        var relWithDestination = getRelationshipDiff("2", "nonexistant-id", getId(component2OwnedByContainer));
        var relWithBoth = getRelationshipDiff("3", getId(component1OwnedByContainer), getId(component2OwnedByContainer));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(
                component1OwnedByContainer,
                component2OwnedByContainer,
                relWithBoth,
                relWithDestination,
                relWithSource
        );
        var actual = diffset.getComponentLevelDiffs(getContainerDiff().getElement().getId());

        assertThat(actual, equalTo(Set.of(component1OwnedByContainer, component2OwnedByContainer, relWithBoth, relWithSource, relWithDestination)));
    }

    @Test
    public void componentLevelDiffsShouldHaveExternalEntities() {
        var component = getComponentDiff("component", getId(getContainerDiff()));
        var componentOutsideContainer = getComponentDiff("other-component", "nonexistant-owner");
        var personOutsideContainer = getPersonDiff("other-person");

        var relWithSource = getRelationshipDiff("1", getId(component), getId(componentOutsideContainer));
        var relWithDestination = getRelationshipDiff("2", getId(personOutsideContainer), getId(component));

        var diffset = getDiffSetWithAllTypesOfDiffsPlus(
                component,
                componentOutsideContainer,
                personOutsideContainer,
                relWithDestination,
                relWithSource
        );
        var actual = diffset.getComponentLevelDiffs(getContainerDiff().getElement().getId());

        assertThat(actual, equalTo(Set.of(component, componentOutsideContainer, personOutsideContainer, relWithSource, relWithDestination)));
    }

    private DiffSet getDiffSetWithAllTypesOfDiffs() {
        return new DiffSet(List.of(
                getRelationshipDiff(),
                getPersonDiff(),
                getSystemDiff(),
                getContainerDiff(),
                getComponentDiff()
        ));
    }

    private DiffSet getDiffSetWithAllTypesOfDiffsPlus(Diff... additionalDiffs) {
        var diffs = new HashSet<>(Set.of(additionalDiffs));
        diffs.addAll(getDiffSetWithAllTypesOfDiffs().getDiffs());
        return new DiffSet(diffs);
    }

    private Diff getPersonDiff() {
        return getPersonDiff("p1");
    }

    private Diff getPersonDiff(String id) {
        return new Diff(
                new DiffableEntity(ArchitectureDataStructureHelper.createPerson(id)),
                null
        );
    }

    private Diff getComponentDiff() {
        return getComponentDiff("comp1", "comp1-system");
    }

    private Diff getComponentDiff(String id, String containerId) {
        return new Diff(
                new DiffableEntity(ArchitectureDataStructureHelper.createComponent(id, containerId)),
                null
        );
    }

    private Diff getContainerDiff() {
        return getContainerDiff("cont1", "cont1-system");
    }

    private Diff getContainerDiff(String id, String systemId) {
        return new Diff(
                new DiffableEntity(ArchitectureDataStructureHelper.createContainer(id, systemId)),
                null
        );
    }

    private Diff getSystemDiff() {
        return getSystemDiff("s1");
    }

    private Diff getSystemDiff(String id) {
        return new Diff(
                new DiffableEntity(ArchitectureDataStructureHelper.createSystem(id)),
                null
        );
    }

    private Diff getRelationshipDiff() {
        return getRelationshipDiff("r1", "r1-source", "r1-dest");
    }

    private Diff getRelationshipDiff(String id, String sourceId, String destinationId) {
        return new Diff(
                new DiffableRelationship(
                        sourceId,
                        ArchitectureDataStructureHelper.createRelationship(id, destinationId)
                ),
                null
        );
    }

    private String getId(Diff d) {
        return d.getElement().getId();
    }
}
