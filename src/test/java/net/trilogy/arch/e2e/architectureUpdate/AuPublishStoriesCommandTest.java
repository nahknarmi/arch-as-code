package net.trilogy.arch.e2e.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateObjectMapper;
import net.trilogy.arch.adapter.architectureYaml.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory;
import net.trilogy.arch.adapter.jira.*;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.facade.FilesFacade;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class AuPublishStoriesCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private File rootDir;
    private Path testCloneDirectory;

    private JiraApi mockedJiraApi;
    private Application app;
    private FilesFacade spiedFilesFacade;
    private GitInterface mockedGitInterface;

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));

        spiedFilesFacade = spy(new FilesFacade());

        rootDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_AU_PUBLISH).getPath());

        GoogleDocsAuthorizedApiFactory mockedGoogleApiFactory = mock(GoogleDocsAuthorizedApiFactory.class);

        final JiraApiFactory mockedJiraApiFactory = mock(JiraApiFactory.class);
        mockedJiraApi = mock(JiraApi.class);
        when(mockedJiraApiFactory.create(spiedFilesFacade, rootDir.toPath())).thenReturn(mockedJiraApi);

        mockedGitInterface = mock(GitInterface.class);

        app = Application.builder()
                .googleDocsAuthorizedApiFactory(mockedGoogleApiFactory)
                .jiraApiFactory(mockedJiraApiFactory)
                .filesFacade(spiedFilesFacade)
                .gitInterface(mockedGitInterface)
                .build();

        Path testDirectory = rootDir.toPath().resolve("architecture-updates/test/");
        testCloneDirectory = rootDir.toPath().resolve("architecture-updates/test-clone/");

        if (!Files.exists(testCloneDirectory)) Files.createDirectory(testCloneDirectory);
        Files.copy(testDirectory.resolve("architecture-update.yml"), testCloneDirectory.resolve("architecture-update.yml"));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(originalOut);
        System.setErr(originalErr);

        Files.deleteIfExists(testCloneDirectory.resolve("architecture-update.yml"));
        Files.deleteIfExists(testCloneDirectory);
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadConfig() throws Exception {
        // Given
        mockGitInterface();
        var newApp = Application.builder().gitInterface(mockedGitInterface).build();

        // When
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        int status = execute(newApp, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("Unable to load configuration.\nError thrown: java.nio.file.NoSuchFileException"));
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadAu() throws Exception {
        // Given
        doThrow(new RuntimeException("ERROR", new RuntimeException("DETAILS")))
                .when(spiedFilesFacade).readString(eq(testCloneDirectory.resolve("architecture-update.yml")));

        // When
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        int status = execute(app, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to load architecture update.\nError thrown: java.lang.RuntimeException: ERROR\nCause: java.lang.RuntimeException: DETAILS\n"));
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadArchitecture() throws Exception {
        // Given
        mockGitInterface();
        doThrow(new RuntimeException("ERROR", new RuntimeException("DETAILS")))
                .when(spiedFilesFacade).readString(eq(rootDir.toPath().resolve("product-architecture.yml")));

        // When
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        int status = execute(app, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to load architecture.\nError thrown: java.lang.RuntimeException: ERROR\nCause: java.lang.RuntimeException: DETAILS\n"));
    }

    @Test
    public void shouldFailGracefullyIfUnableToCreateJiraStoryDTO() throws Exception {
        // Given
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult("PROJ_ID", "PROJ_KEY");
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray())).thenReturn(epicInformation);
        mockGitInterface();

        // When
        String command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/invalid-story/ " + rootDir.getAbsolutePath();
        int status = execute(app, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(
                out.toString(),
                equalTo(
                        "Not re-creating stories:\n  - story that should not be created\n\n" +
                                "Checking epic...\n\n" +
                                "Attempting to create stories...\n\n"
                )
        );
        collector.checkThat(err.toString(), equalTo("ERROR: Some stories are invalid. Please run 'au validate' command.\n"));
    }

    @Test
    public void shouldQueryJiraForEpic() throws Exception {
        // Given
        Jira epic = new Jira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");
        mockGitInterface();

        // When
        String command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/test-clone/ " + rootDir.getAbsolutePath();
        execute(app, command);

        // Then
        verify(mockedJiraApi).getStory(epic, "user", "password".toCharArray());
    }

    @Test
    public void shouldTellJiraToCreateStories() throws Exception {
        // GIVEN:
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult("PROJ_ID", "PROJ_KEY");
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray())).thenReturn(epicInformation);
        mockGitInterface();

        // WHEN:
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        execute(app, command);

        // THEN:
        List<JiraStory> expected = getExpectedJiraStoriesToCreate();
        verify(mockedJiraApi).createStories(expected, epic.getTicket(), epicInformation.getProjectId(), epicInformation.getProjectKey(), "user", "password".toCharArray());
    }

    @Test
    public void shouldOutputResult() throws Exception {
        // GIVEN:
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult("PROJ_ID", "PROJ_KEY");
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray()))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        JiraCreateStoryStatus.succeeded("ABC-123", "link-to-ABC-123"),
                        JiraCreateStoryStatus.succeeded("ABC-223", "link-to-ABC-223")
                ));
        mockGitInterface();

        // WHEN:
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        execute(app, command);

        // THEN:
        collector.checkThat(
                out.toString(),
                equalTo(
                        "Not re-creating stories:\n  - story that should not be created\n\n" +
                                "Checking epic...\n\n" +
                                "Attempting to create stories...\n\n" +
                                "Successfully created:\n  - story that should be created\n  - story that failed to be created\n"
                )
        );
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldUpdateAuWithNewJiraTickets() throws Exception {
        // GIVEN:
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult("PROJ_ID", "PROJ_KEY");
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray()))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        JiraCreateStoryStatus.succeeded("ABC-123", "link-to-ABC-123"),
                        JiraCreateStoryStatus.failed("error-message")
                ));
        mockGitInterface();

        // WHEN:
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        execute(app, command);
        String actualAuAsstring = Files.readString(testCloneDirectory.resolve("architecture-update.yml"));
        ArchitectureUpdate actualAu = new ArchitectureUpdateObjectMapper().readValue(actualAuAsstring);

        // THEN:
        String originalAuAsString = Files.readString(testCloneDirectory.resolve("architecture-update.yml"));
        ArchitectureUpdate originalAu = new ArchitectureUpdateObjectMapper().readValue(originalAuAsString);
        ArchitectureUpdate expectedAu = originalAu.addJiraToFeatureStory(
                originalAu.getCapabilityContainer().getFeatureStories().get(0), new Jira("ABC-123", "link-to-ABC-123")
        );

        collector.checkThat(actualAu, equalTo(expectedAu));
    }

    @Test
    public void shouldDisplayPartialErrorsWhenCreatingStories() throws Exception {
        // GIVEN:
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult("PROJ_ID", "PROJ_KEY");
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray()))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(any(), any(), any(), any(), any(), any()))
                .thenReturn(
                        List.of(JiraCreateStoryStatus.succeeded("ABC-123", "link-to-ABC-123"),
                                JiraCreateStoryStatus.failed("error-message"))
                );
        mockGitInterface();

        // WHEN:
        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        int statusCode = execute(app, command);

        // THEN:
        assertThat(
                err.toString(),
                equalTo("\nError! Some stories failed to publish. Please retry. Errors reported by Jira:\n\nStory: \"story that failed to be created\":\n  - error-message\n")
        );
        assertThat(
                out.toString(),
                equalTo("Not re-creating stories:\n  - story that should not be created\n\n" +
                        "Checking epic...\n\n" +
                        "Attempting to create stories...\n\nSuccessfully created:\n  - story that should be created\n")
        );
        assertThat(statusCode, equalTo(0));
    }

    @Test
    public void shouldDisplayNiceErrorIfCreatingStoriesCrashes() throws Exception {
        when(mockedJiraApi.getStory(any(), any(), any()))
                .thenReturn(new JiraQueryResult("ABC", "DEF"));
        when(mockedJiraApi.createStories(any(), any(), any(), any(), any(), any()))
                .thenThrow(JiraApi.JiraApiException.builder().message("OOPS!").cause(new RuntimeException("Details")).build());
        mockGitInterface();

        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        Integer statusCode = execute(app, command);

        assertThat(err.toString(), equalTo("Jira API failed\nError thrown: net.trilogy.arch.adapter.jira.JiraApi$JiraApiException: OOPS!\nCause: java.lang.RuntimeException: Details\n"));
        assertThat(
                out.toString(),
                equalTo(
                        "Not re-creating stories:\n  - story that should not be created\n\n" +
                                "Checking epic...\n\n" +
                                "Attempting to create stories...\n\n"
                )
        );
        assertThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldHandleNoStoriesToCreate() throws Exception {
        mockGitInterface();

        String command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/no-stories-to-create/ " + rootDir.getAbsolutePath();
        Integer statusCode = execute(app, command);
        verifyNoMoreInteractions(mockedJiraApi);

        collector.checkThat(err.toString(), equalTo("ERROR: No stories to create.\n"));
        collector.checkThat(out.toString(), equalTo("Not re-creating stories:\n  - story that should not be created\n\n"));
        collector.checkThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldDisplayGetStoryErrorsFromJira() throws Exception {
        Jira epic = new Jira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");

        mockGitInterface();
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray()))
                .thenThrow(JiraApi.JiraApiException.builder().message("OOPS!").build());

        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        Integer statusCode = execute(app, command);

        assertThat(err.toString(), equalTo("Jira API failed\nError thrown: net.trilogy.arch.adapter.jira.JiraApi$JiraApiException: OOPS!\n"));
        assertThat(out.toString(), equalTo("Not re-creating stories:\n  - story that should not be created\n\nChecking epic...\n\n"));
        assertThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldGracefullyHandleErrorsInGitInterface() throws Exception {
        when(mockedGitInterface.load(any(), any()))
                .thenThrow(new RuntimeException("Boom!"));

        String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        final Integer status = execute(app, command);

        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to load product architecture in branch: master\nError thrown: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldGracefullyHandleAuUpdateWriteFailure() throws Exception {
        // GIVEN:
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult("PROJ_ID", "PROJ_KEY");
        when(mockedJiraApi.getStory(epic, "user", "password".toCharArray()))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(any(), any(), any(), any(), any(), any()))
                .thenReturn(
                        List.of(JiraCreateStoryStatus.succeeded("ABC-123", "link-to-ABC-123"),
                                JiraCreateStoryStatus.succeeded("ABC-223", "link-to-ABC-223"))
                );
        mockGitInterface();
        doThrow(new RuntimeException("ERROR", new RuntimeException("Boom!"))).when(spiedFilesFacade).writeString(any(), any());

        // WHEN:
        final String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        final Integer status = execute(app, command);

        // THEN:
        collector.checkThat(
                err.toString(),
                equalTo("Unable to write update to AU.\nError thrown: java.lang.RuntimeException: ERROR\nCause: java.lang.RuntimeException: Boom!\n")
        );
        collector.checkThat(status, not(equalTo(0)));
    }

    private List<JiraStory> getExpectedJiraStoriesToCreate() {
        return List.of(
                new JiraStory(
                        "story that should be created",
                        List.of(
                                new JiraStory.JiraTdd(
                                        new Tdd.Id("[SAMPLE-TDD-ID]"),
                                        new Tdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/Reset Password Controller"
                                ),
                                new JiraStory.JiraTdd(
                                        new Tdd.Id("[SAMPLE-TDD-ID-2]"),
                                        new Tdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/E-mail Component"
                                )
                        ),
                        List.of(
                                new JiraStory.JiraFunctionalRequirement(
                                        new FunctionalRequirement.Id("[SAMPLE-REQUIREMENT-ID]"),
                                        new FunctionalRequirement(
                                                "[SAMPLE REQUIREMENT TEXT]",
                                                "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                                List.of(new Tdd.Id("[SAMPLE-TDD-ID]"))
                                        )
                                )
                        )
                ),
                new JiraStory(
                        "story that failed to be created",
                        List.of(
                                new JiraStory.JiraTdd(
                                        new Tdd.Id("[SAMPLE-TDD-ID]"),
                                        new Tdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/Reset Password Controller"
                                )
                        ),
                        List.of(
                                new JiraStory.JiraFunctionalRequirement(
                                        new FunctionalRequirement.Id("[SAMPLE-REQUIREMENT-ID]"),
                                        new FunctionalRequirement(
                                                "[SAMPLE REQUIREMENT TEXT]",
                                                "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                                List.of(new Tdd.Id("[SAMPLE-TDD-ID]"))
                                        )
                                )
                        )
                )
        );
    }

    private void mockGitInterface() throws IOException, GitAPIException, GitInterface.BranchNotFoundException {
        when(mockedGitInterface.load("master", rootDir.toPath().resolve("product-architecture.yml")))
                .thenReturn(
                        new ArchitectureDataStructureObjectMapper()
                                .readValue(
                                        Files.readString(
                                                rootDir.toPath().resolve("product-architecture.yml"))
                                                .replaceAll("34", "DELETED-COMPONENT-ID")
                                )
                );
    }
}

