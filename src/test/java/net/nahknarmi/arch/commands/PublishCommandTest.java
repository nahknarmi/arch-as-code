package net.nahknarmi.arch.commands;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PublishCommandTest {

    @Test
    public void publish() throws Exception {
        PublishCommand publishCommand = new PublishCommand();
        publishCommand.productDocumentationRoot = new File("./documentation/products/arch-as-code");

        Integer statusCode = publishCommand.call();

        assertThat(statusCode, equalTo(0));
    }
}