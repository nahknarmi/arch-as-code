package net.trilogy.arch.domain.c4;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static net.trilogy.arch.domain.c4.C4Action.DELIVERS;
import static net.trilogy.arch.domain.c4.C4Path.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

public class C4ModelTest {
    @Test
    public void fails_to_add_same_person_twice() {
        final var model = new C4Model()
                .addPerson(C4Person.builder().name("foo").description("bar").id("1").build());

        assertThrows(IllegalArgumentException.class, () ->
                model.addPerson(C4Person.builder().name("foo").description("bar").id("1").build()));
    }

    @Test
    public void fails_to_add_same_system_twice() {
        final var model = new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().name("foo").description("bar").id("1").build());

        assertThrows(IllegalArgumentException.class, () ->
                model.addSoftwareSystem(C4SoftwareSystem.builder().name("foo").description("bar").id("1").build()));
    }

    @Test
    @Ignore("TODO: It is the FIRST model call which throws, not the second")
    public void fails_to_add_same_container_twice() {
        final var model = new C4Model()
                .addContainer(C4Container.builder().name("foo").description("bar").id("1").technology("tech").build());

        assertThrows(IllegalArgumentException.class, () ->
                model.addContainer(C4Container.builder().name("foo").description("bar").id("1").technology("tech").build()));
    }

    @Test
    @Ignore("TODO: It is the FIRST model call which throws, not the second")
    public void fails_to_add_same_component_twice() {
        final var model = new C4Model()
                .addComponent(C4Component.builder().name("foo").description("bar").id("1").technology("tech").build());

        assertThrows(IllegalArgumentException.class, () ->
                model.addComponent(C4Component.builder().name("foo").description("bar").id("1").technology("tech").build()));
    }

    @Test
    public void should_add_person_if_it_doesnt_already_exist_in_model() {
        new C4Model()
                .addPerson(C4Person.builder().name("foo").description("bar").id("1").build());
    }

    @Test
    public void should_add_system_if_it_doesnt_already_exist_in_model() {
        new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().name("foo").description("bar").id("1").build());
    }

    @Test
    public void fails_to_add_two_people_with_same_name() {
        final var model = new C4Model()
                .addPerson(C4Person.builder().name("John").description("bar").id("1").build());

        assertThrows(IllegalArgumentException.class, () ->
                model.addPerson(C4Person.builder().name("John").description("bar").id("2").build()));
    }

    @Test
    public void fails_to_add_two_systems_with_same_name() {
        final var model = new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().name("OBP").description("bar").id("1").build());

        assertThrows(IllegalArgumentException.class, () ->
                model.addSoftwareSystem(C4SoftwareSystem.builder().name("OBP").description("bar").id("2").build()));
    }

    @Test
    public void find_by_path() {
        C4Model c4Model = new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder()
                        .path(path("c4://OBP"))
                        .name("OBP")
                        .description("bar")
                        .id("1")
                        .build());

        assertThat(c4Model.findByPath(path("c4://OBP")).getId(), equalTo("1"));
    }

    @Test
    public void find_by_id() {
        C4Model c4Model = new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder()
                        .path(path("c4://OBP"))
                        .name("OBP")
                        .description("bar")
                        .id("1")
                        .build());

        assertThat(c4Model.findEntityById("1").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "1")).getId(), equalTo("1"));
    }

    @Test
    public void find_by_alias() {
        C4Model c4Model = new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder()
                        .path(path("c4://OBP"))
                        .name("OBP")
                        .alias("OBP")
                        .description("bar")
                        .id("1")
                        .build());

        assertThat(c4Model.findEntityByAlias("OBP").getId(), equalTo("1"));
    }

    @Test
    public void find_person_by_name() {
        C4Model c4Model = new C4Model()
                .addPerson(C4Person.builder()
                        .path(path("@bob"))
                        .name("Bob")
                        .alias("bobby")
                        .description("bar")
                        .id("1")
                        .build());

        assertThat(c4Model.findPersonByName("Bob").getId(), equalTo("1"));
    }

    @Test
    public void find_relation_by_id() {
        C4Model c4Model = new C4Model()
                .addPerson(C4Person.builder()
                        .path(path("@bob"))
                        .name("Bob")
                        .alias("bobby")
                        .description("bar")
                        .id("2")
                        .build())
                .addSoftwareSystem(C4SoftwareSystem.builder()
                        .name("OBP")
                        .description("bar")
                        .id("1")
                        .relationship(C4Relationship.builder()
                                .id("3")
                                .alias("relation")
                                .action(DELIVERS)
                                .withAlias("bobby")
                                .description("desc")
                                .build())
                        .build());

        assertThat(c4Model.findRelationshipById("3").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "3")).getId(), equalTo("3"));
    }

    @Test
    public void find_relation_by_alias() {
        C4Model c4Model = new C4Model()
                .addPerson(C4Person.builder()
                        .path(path("@bob"))
                        .name("Bob")
                        .alias("bobby")
                        .description("bar")
                        .id("2")
                        .build())
                .addSoftwareSystem(C4SoftwareSystem.builder()
                        .name("OBP")
                        .description("bar")
                        .id("1")
                        .relationship(C4Relationship.builder()
                                .id("3")
                                .alias("relation")
                                .action(DELIVERS)
                                .withAlias("bobby")
                                .description("desc")
                                .build())
                        .build());

        assertThat(c4Model.findRelationshipByAlias("relation").getId(), equalTo("3"));
    }

    @Test
    public void find_entity_by_relationship_with() {
        C4Relationship relationship = C4Relationship.builder()
                .id("3")
                .alias("relation")
                .action(DELIVERS)
                .withAlias("bobby")
                .description("desc")
                .build();

        C4Model c4Model = new C4Model()
                .addPerson(C4Person.builder()
                        .path(path("@bob"))
                        .name("Bob")
                        .alias("bobby")
                        .description("bar")
                        .id("2")
                        .build())
                .addSoftwareSystem(C4SoftwareSystem.builder()
                        .name("OBP")
                        .description("bar")
                        .id("1")
                        .relationship(relationship)
                        .build());

        assertThat(c4Model.findEntityByRelationshipWith(relationship).getId(), equalTo("2"));
    }

    @Test
    public void find_entity_by_reference() {
        C4Model c4Model = new C4Model()
                .addPerson(C4Person.builder()
                        .path(path("@bob"))
                        .name("Bob")
                        .alias("bobby")
                        .description("bar")
                        .id("2")
                        .build());

        C4Reference idRef = new C4Reference("2", null);
        C4Reference aliasRef = new C4Reference(null, "bobby");

        assertThat(c4Model.findEntityByReference(idRef).getId(), equalTo("2"));
        assertThat(c4Model.findEntityByReference(aliasRef).getId(), equalTo("2"));
    }

    @Test
    public void find_with_tag() {
        final var tagToFind = new C4Tag("YOU'RE IT");
        final var tagToIgnore = new C4Tag("SORRY, NOT YOU");
        final var c4Model = new C4Model()
                .addPerson(C4Person.builder()
                        .path(path("@alice"))
                        .name("Alice")
                        .tag(tagToFind)
                        .description("first person")
                        .id("2")
                        .build())
                .addPerson(C4Person.builder()
                        .path(path("@bob"))
                        .name("Bob")
                        .tag(tagToIgnore)
                        .description("second person")
                        .id("3")
                        .build())
                .addPerson(C4Person.builder()
                        .path(path("@carol"))
                        .name("Carol")
                        .tag(tagToFind)
                        .tag(tagToIgnore)
                        .description("third person")
                        .id("4")
                        .build());

        assertThat(c4Model.findWithTag(tagToFind).stream().map(Entity::getName).collect(toSet()),
                equalTo(Set.of("Alice", "Carol")));
    }
}
