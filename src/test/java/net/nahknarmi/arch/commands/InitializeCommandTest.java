package net.nahknarmi.arch.commands;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;


public class InitializeCommandTest {
    private Path tempProductDirectory;

    @Before
    public void setUp() throws Exception {
        tempProductDirectory = Files.createTempDirectory("arch-as-code");
    }

    @After
    public void tearDown() throws Exception {
        Files.walk(tempProductDirectory).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void initialize() throws Exception {
        InitializeCommand command = new InitializeCommand();
        command.apiKey = "key";
        command.apiSecret = "secret";
        command.workspaceId = "1234";
        command.productDocumentationRoot = tempProductDirectory.toFile();

        assertThat(command.call(), equalTo(0));

        //check that credentials.json file created
        assertTrue(tempProductDirectory.resolve(".arch-as-code/structurizr/credentials.json").toFile().exists());

        //check that data-structure.yml file created
        assertTrue(tempProductDirectory.resolve("data-structure.yml").toFile().exists());
    }
}