package net.nahknarmi.arch.domain.c4;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class C4PathTest {


    @Test
    public void system() {
        C4Path path = new C4Path("c4://DevSpaces");


        assertThat(path.getName(), equalTo("DevSpaces"));
        assertThat(path.getSystemName(), equalTo("DevSpaces"));
        assertThat(path.getType(), equalTo(C4Type.system));
    }


    @Test
    public void person() {

        C4Path path = new C4Path("@PCA");

        assertThat(path.getName(), equalTo("PCA"));
        assertThat(path.getType(), equalTo(C4Type.person));
    }
}