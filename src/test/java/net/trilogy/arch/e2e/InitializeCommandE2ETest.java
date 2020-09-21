package net.trilogy.arch.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.trilogy.arch.Application;
import net.trilogy.arch.CommandTestBase;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.file.Files.createTempDirectory;
import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class InitializeCommandE2ETest extends CommandTestBase {
    private Path tempProductDirectory;

    @Before
    public void setUp() throws Exception {
        tempProductDirectory = createTempDirectory("arch-as-code");
    }

    @After
    public void tearDown() throws Exception {
        //noinspection ResultOfMethodCallIgnored
        Files.walk(tempProductDirectory).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void shouldInitializeCredentials() throws Exception {
        final int status = execute("init -i key -k secret -s 1234 " + tempProductDirectory.toAbsolutePath());
        collector.checkThat(status, equalTo(0));

        File file = tempProductDirectory.resolve(".arch-as-code/structurizr/credentials.json").toFile();
        collector.checkThat(file.exists(), equalTo(true));
        collector.checkThat(file.isFile(), equalTo(true));

        final var expected = new ObjectMapper().readValue(
                file,
                new TypeReference<Map<String, String>>() {
                });

        // TODO: Fix to use `collector`
        assertEquals(Map.of(
                "workspace_id", "key",
                "api_key", "secret",
                "api_secret", "1234"),
                expected);
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
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString("Unable to initialize\nError: java.io.IOException: Boom!"));
    }
}
