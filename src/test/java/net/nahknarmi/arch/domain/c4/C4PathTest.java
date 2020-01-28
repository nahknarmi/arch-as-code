package net.nahknarmi.arch.domain.c4;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class C4PathTest {

    @Test
    public void person() {

        C4Path path = new C4Path("@PCA");

        assertThat(path.getName(), equalTo("PCA"));
        assertThat(path.getType(), equalTo(C4Type.person));
    }

    @Test
    public void system() {
        C4Path path = new C4Path("c4://DevSpaces");

        assertThat(path.getName(), equalTo("DevSpaces"));
        assertThat(path.getSystemName(), equalTo("DevSpaces"));
        assertThat(path.getType(), equalTo(C4Type.system));
    }

    @Test
    public void container() {
        C4Path path = new C4Path("c4://DevSpaces/DevSpaces API");

        assertThat(path.getName(), equalTo("DevSpaces API"));
        assertThat(path.getSystemName(), equalTo("DevSpaces"));
        assertThat(path.getContainerName(), equalTo(Optional.of("DevSpaces API")));

        assertThat(path.getType(), equalTo(C4Type.container));
    }

    @Test
    public void component() {
        C4Path path = new C4Path("c4://DevSpaces/DevSpaces API/Sign-In Component");

        assertThat(path.getName(), equalTo("Sign-In Component"));
        assertThat(path.getSystemName(), equalTo("DevSpaces"));
        assertThat(path.getContainerName(), equalTo(Optional.of("DevSpaces API")));
        assertThat(path.getComponentName(), equalTo(Optional.of("Sign-In Component")));

        assertThat(path.getType(), equalTo(C4Type.component));
    }

    @Test(expected = IllegalArgumentException.class)
    public void missing_person() {
        new C4Path("@");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missing_system() {
        new C4Path("c4://");
    }
}
