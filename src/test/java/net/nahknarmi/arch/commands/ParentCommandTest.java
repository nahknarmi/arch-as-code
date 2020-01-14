package net.nahknarmi.arch.commands;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ParentCommandTest {

    @Test
    public void parent() {
        Integer statusCode = new ParentCommand().call();
        assertThat(statusCode, equalTo(0));
    }
}