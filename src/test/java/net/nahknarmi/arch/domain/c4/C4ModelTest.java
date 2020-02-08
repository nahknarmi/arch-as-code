package net.nahknarmi.arch.domain.c4;

import com.google.common.collect.ImmutableSet;
import org.junit.Ignore;
import org.junit.Test;

public class C4ModelTest {


    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void should_not_be_able_to_add_same_person_twice() {

        C4Path path = new C4Path("@Foo");
        C4Model.builder()
                .people(ImmutableSet.of(C4Person.builder().description("bar").path(path).build(), C4Person.builder().description("bar").path(path).build()))
                .build();
    }
}