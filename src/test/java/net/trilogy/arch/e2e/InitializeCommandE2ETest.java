package net.trilogy.arch.e2e;


import net.trilogy.arch.Application;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class InitializeCommandE2ETest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private Path tempProductDirectory;

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        tempProductDirectory = Files.createTempDirectory("arch-as-code");

        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void tearDown() throws Exception {
        Files.walk(tempProductDirectory).map(Path::toFile).forEach(File::delete);

        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void shouldInitializeCredentials() throws Exception {
        Integer status = execute("init -i key -k secret -s 1234 " + tempProductDirectory.toAbsolutePath());
        collector.checkThat(status, equalTo(0));

        File file = tempProductDirectory.resolve(".arch-as-code/structurizr/credentials.json").toFile();
        collector.checkThat(file.exists(), equalTo(true));
        collector.checkThat(file.isFile(), equalTo(true));
        collector.checkThat(Files.readAllLines(file.toPath()),
                contains("{\"workspace_id\":\"key\",\"api_key\":\"secret\",\"api_secret\":\"1234\"}"));
    }

    @Test
    public void shouldInitializeDataStructureYamlFile() throws Exception {
        Integer status = execute("init -i key -k secret -s 1234 " + tempProductDirectory.toAbsolutePath());
        collector.checkThat(status, equalTo(0));

        File file = tempProductDirectory.resolve("product-architecture.yml").toFile();
        collector.checkThat(file.exists(), equalTo(true));
        collector.checkThat(Files.readAllLines(file.toPath()),
                contains(
                        "name: Hello World!!!",
                        "businessUnit: DevFactory",
                        "description: Architecture as code",
                        "decisions: []",
                        "model:",
                        "  people: []",
                        "  systems: []",
                        "  containers: []",
                        "  components: []",
                        "  deploymentNodes: []"
                )
        );
    }

    @Test
    public void shouldGracefullyLogErrors() throws Exception {
        // Given
        final FilesFacade mockedFilesFacade = Mockito.mock(FilesFacade.class);
        when(mockedFilesFacade.writeString(any(), any())).thenThrow(new IOException("Boom!"));
        final Application app = Application.builder()
                .filesFacade(mockedFilesFacade)
                .build();

        // When
        Integer status = execute(app, "init -i key -k secret -s 1234 " + tempProductDirectory.toAbsolutePath());

        collector.checkThat(status, not(0));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("Unable to initialize\nError thrown: java.io.IOException: Boom!"));
    }
}
