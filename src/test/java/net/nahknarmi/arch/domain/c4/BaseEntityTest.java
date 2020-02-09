package net.nahknarmi.arch.domain.c4;

import org.junit.Test;

import static net.nahknarmi.arch.domain.c4.C4Path.path;

public class BaseEntityTest {

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_assign_invalid_path_for_person_type() {
        C4Person.builder().path(path("c4://abc")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_assign_invalid_path_for_system_type() {
        C4SoftwareSystem.builder().path(path("@Foo")).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void fails_to_assign_invalid_path_for_container_type() {
        C4Container.builder().path(path("@Foo")).build();
    }
}