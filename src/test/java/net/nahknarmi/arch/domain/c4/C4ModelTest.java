package net.nahknarmi.arch.domain.c4;

import org.junit.Test;

import static net.nahknarmi.arch.domain.c4.C4Path.path;

public class C4ModelTest {

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_same_person_twice() {
        C4Path path = path("@Foo");
        new C4Model()
                .addPerson(C4Person.builder().description("bar").path(path).build())
                .addPerson(C4Person.builder().description("bar").path(path).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_same_system_twice() {
        C4Path path = path("c4://sys1");
        new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().description("bar").path(path).build())
                .addSoftwareSystem(C4SoftwareSystem.builder().description("bar").path(path).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_same_container_twice() {
        C4Path path = path("c4://sys1/container");
        new C4Model()
                .addContainer(C4Container.builder().description("bar").path(path).build())
                .addContainer(C4Container.builder().description("bar").path(path).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_same_conomponent_twice() {
        C4Path path = path("c4://sys1/container/component");
        new C4Model()
                .addComponent(C4Component.builder().description("bar").path(path).build())
                .addComponent(C4Component.builder().description("bar").path(path).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_container_to_model_when_system_doesnt_exist() {
        C4Path path = path("c4://sys1/container");
        new C4Model()
                .addContainer(C4Container.builder().description("bar").path(path).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_component_to_model_when_container_doesnt_exist() {
        C4Path systemPath = path("c4://sys1");
        C4Path componentPath = path("c4://sys1/container/component1");
        new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().description("bar").path(systemPath).build())
                .addComponent(C4Component.builder().description("bar").path(componentPath).build());
    }

    @Test
    public void should_add_container_if_system_exists_in_model() {
        C4Path systemPath = path("c4://sys1");
        C4Path containerPath = path("c4://sys1/container");
        new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().description("bar").path(systemPath).build())
                .addContainer(C4Container.builder().description("bar").path(containerPath).build());
    }

    @Test
    public void should_add_component_if_container_exists_in_model() {
        C4Path systemPath = path("c4://sys1");
        C4Path containerPath = path("c4://sys1/container");
        C4Path componentPath = path("c4://sys1/container/component");
        new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().description("bar").path(systemPath).build())
                .addContainer(C4Container.builder().description("bar").path(containerPath).build())
                .addComponent(C4Component.builder().description("bar").path(componentPath).build());
    }

    @Test
    public void should_add_person_if_it_doesnt_already_exist_in_model() {
        C4Path personPath = path("@Foo");
        new C4Model()
                .addPerson(C4Person.builder().description("bar").path(personPath).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_two_people_with_same_name() {
        new C4Model()
                .addPerson(C4Person.builder().name("John").description("bar").path(path("@Foo")).build())
                .addPerson(C4Person.builder().name("John").description("bar").path(path("@Bazz")).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_add_two_systems_with_same_name() {
        new C4Model()
                .addSoftwareSystem(C4SoftwareSystem.builder().name("OBP").description("bar").path(path("c4://sys1")).build())
                .addSoftwareSystem(C4SoftwareSystem.builder().name("OBP").description("bar").path(path("c4://sys2")).build());
    }

}