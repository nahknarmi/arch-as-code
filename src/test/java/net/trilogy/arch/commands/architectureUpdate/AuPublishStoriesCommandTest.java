package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory;
import net.trilogy.arch.adapter.jira.JiraApi;
import net.trilogy.arch.adapter.jira.JiraApi.JiraApiException;
import net.trilogy.arch.adapter.jira.JiraApiFactory;
import net.trilogy.arch.adapter.jira.JiraQueryResult;
import net.trilogy.arch.adapter.jira.JiraStory;
import net.trilogy.arch.adapter.jira.JiraStory.JiraFunctionalRequirement;
import net.trilogy.arch.adapter.jira.JiraStory.JiraTdd;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.String.format;
import static java.lang.System.setErr;
import static java.lang.System.setOut;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.trilogy.arch.TestHelper.execute;
import static net.trilogy.arch.Util.first;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.newJiraApi;
import static net.trilogy.arch.adapter.jira.JiraCreateStoryStatus.failed;
import static net.trilogy.arch.adapter.jira.JiraCreateStoryStatus.succeeded;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.ARCHITECTURE_UPDATE_YML;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AuPublishStoriesCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private File rootDir;
    private Path testCloneDirectory;
    private JiraApi mockedJiraApi;
    private Application app;
    private FilesFacade spiedFilesFacade;
    private GitInterface mockedGitInterface;
    private MockedStatic<JiraApiFactory> mockedJiraApiFactory;

    @Before
    public void setUp() throws Exception {
        mockedJiraApi = mock(JiraApi.class);
        mockedJiraApiFactory = mockStatic(JiraApiFactory.class, invocation -> mockedJiraApi);

        out.reset();
        err.reset();
        setOut(new PrintStream(out));
        setErr(new PrintStream(err));

        spiedFilesFacade = spy(new FilesFacade());

        rootDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_AU_PUBLISH).getPath());

        GoogleDocsAuthorizedApiFactory mockedGoogleApiFactory = mock(GoogleDocsAuthorizedApiFactory.class);

        when(newJiraApi(spiedFilesFacade, rootDir.toPath(), "BOB", "NANCY".toCharArray()))
                .thenReturn(mockedJiraApi);

        mockedGitInterface = mock(GitInterface.class);

        app = Application.builder()
                .googleDocsAuthorizedApiFactory(mockedGoogleApiFactory)
                .filesFacade(spiedFilesFacade)
                .gitInterface(mockedGitInterface)
                .build();

        Path testDirectory = rootDir.toPath().resolve("architecture-updates/test/");
        testCloneDirectory = rootDir.toPath().resolve("architecture-updates/test-clone/");

        if (!Files.exists(testCloneDirectory))
            Files.createDirectory(testCloneDirectory);

        copy(testDirectory.resolve(ARCHITECTURE_UPDATE_YML), testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML));
    }

    @After
    public void tearDown() throws Exception {
        mockedJiraApiFactory.close();

        setOut(originalOut);
        setErr(originalErr);

        deleteIfExists(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML));
        deleteIfExists(testCloneDirectory);
    }

    @Ignore("TODO: Wrong STDERR output")
    @Test
    public void shouldFailGracefullyIfFailToLoadConfig() throws Exception {
        // Given
        mockGitInterface();
        final var newApp = Application.builder()
                .gitInterface(mockedGitInterface)
                .build();

        // When
        final String command = command();
        final int status = execute(newApp, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(
                err.toString(),
                containsString("Unable to load configuration.\nError: java.nio.file.NoSuchFileException"));
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadAu() throws Exception {
        // Given a filesystem read error
        doThrow(new RuntimeException("ERROR", new RuntimeException("DETAILS")))
                .when(spiedFilesFacade).readString(eq(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML)));

        // When
        final String command = command();
        final int status = execute(app, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(
                err.toString(),
                equalTo("Unable to load architecture update.\nError: java.lang.RuntimeException: ERROR\nCause: java.lang.RuntimeException: DETAILS\n"));
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadArchitecture() throws Exception {
        // Given
        mockGitInterface();
        doThrow(new RuntimeException("ERROR", new RuntimeException("DETAILS")))
                .when(spiedFilesFacade).readString(eq(rootDir.toPath().resolve("product-architecture.yml")));

        // When
        final String command = command();
        final int status = execute(app, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(
                err.toString(),
                equalTo("Unable to load architecture.\nError: java.lang.RuntimeException: ERROR\nCause: java.lang.RuntimeException: DETAILS\n"));
    }

    @Test
    public void shouldFailGracefullyIfUnableToCreateJiraStoryDTO() throws Exception {
        // Given
        final var epic = Jira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // When
        final var command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/invalid-story/ " + rootDir.getAbsolutePath();
        final int status = execute(app, command);

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(
                out.toString(),
                equalTo(
                        "Not re-creating stories:\n  - story that should not be created\n\n" +
                                "Checking epic...\n\n" +
                                "Attempting to create stories...\n\n"));
        collector.checkThat(err.toString(), equalTo("ERROR: Some stories are invalid. Please run 'au validate' command.\n"));
    }

    @Test
    public void shouldQueryJiraForEpic() throws Exception {
        // Given
        final var epic = new Jira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");
        mockGitInterface();

        // When
        final var command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/test-clone/ " + rootDir.getAbsolutePath();
        execute(app, command);

        // Then
        verify(mockedJiraApi).getStory(epic);
    }

    @Test
    public void shouldTellJiraToCreateStories() throws Exception {
        // GIVEN:
        final var epic = Jira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // WHEN:
        final String command = command();
        execute(app, command);

        // THEN:
        final var expected = getExpectedJiraStoriesToCreate();
        verify(mockedJiraApi).createStories(expected, epic.getTicket(), epicInformation.getProjectId());
    }

    @Test
    public void shouldTellJiraToCreateStoriesWithTddContent() throws Exception {
        // GIVEN:
        final var epic = Jira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // WHEN:
        final var command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/tdd-content/ " + rootDir.getAbsolutePath();
        execute(app, command);

        // THEN:
        final var expected = getExpectedJiraStoriesWithTddContentToCreate();
        verify(mockedJiraApi).createStories(expected, epic.getTicket(), epicInformation.getProjectId());
    }

    @Test
    public void shouldOutputResult() throws Exception {
        // GIVEN:
        final var epic = Jira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(anyList(), anyString(), anyLong()))
                .thenReturn(List.of(
                        succeeded("ABC-123", "link-to-ABC-123"),
                        succeeded("ABC-223", "link-to-ABC-223")));
        mockGitInterface();

        // WHEN:
        final String command = command();
        execute(app, command);

        // THEN:
        collector.checkThat(
                out.toString(),
                equalTo(
                        "Not re-creating stories:\n  - story that should not be created\n\n" +
                                "Checking epic...\n\n" +
                                "Attempting to create stories...\n\n" +
                                "Successfully created:\n  - story that should be created\n  - story that failed to be created\n"));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldUpdateAuWithNewJiraTickets() throws Exception {
        // GIVEN:
        final var epic = Jira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(anyList(), anyString(), anyLong())).thenReturn(List.of(
                succeeded("ABC-123", "link-to-ABC-123"),
                failed("error-message")));
        mockGitInterface();

        // WHEN:
        final String command = command();
        execute(app, command);
        final var actualAuAsstring = Files.readString(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML));
        final var actualAu = YAML_OBJECT_MAPPER.readValue(actualAuAsstring, ArchitectureUpdate.class);

        // THEN:
        final var originalAuAsString = Files.readString(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML));
        final var originalAu = YAML_OBJECT_MAPPER.readValue(originalAuAsString, ArchitectureUpdate.class);
        final var expectedAu = originalAu.addJiraToFeatureStory(
                first(originalAu.getCapabilityContainer().getFeatureStories()), new Jira("ABC-123", "link-to-ABC-123"));

        collector.checkThat(actualAu, equalTo(expectedAu));
    }

    @Test
    public void shouldDisplayPartialErrorsWhenCreatingStories() throws Exception {
        // GIVEN:
        final var epic = Jira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(anyList(), anyString(), anyLong()))
                .thenReturn(List.of(
                        succeeded("ABC-123", "link-to-ABC-123"),
                        failed("error-message")));
        mockGitInterface();

        // WHEN:
        final String command = command();
        final int statusCode = execute(app, command);

        // THEN:
        assertThat(
                err.toString(),
                equalTo("\nError! Some stories failed to publish. Please retry. Errors reported by Jira:\n\nStory: \"story that failed to be created\":\n  - error-message\n"));
        assertThat(
                out.toString(),
                equalTo("Not re-creating stories:\n  - story that should not be created\n\n" +
                        "Checking epic...\n\n" +
                        "Attempting to create stories...\n\nSuccessfully created:\n  - story that should be created\n"));
        assertThat(statusCode, equalTo(0));
    }

    @Test
    public void shouldDisplayNiceErrorIfCreatingStoriesCrashes() throws Exception {
        when(mockedJiraApi.getStory(any()))
                .thenReturn(new JiraQueryResult(1L, "DEF"));
        when(mockedJiraApi.createStories(anyList(), anyString(), anyLong()))
                .thenThrow(new JiraApiException(
                        "OOPS!",
                        new RuntimeException("Details")));
        mockGitInterface();

        final String command = command();
        final int statusCode = execute(app, command);

        assertThat(err.toString(), equalTo("Jira API failed\nError: net.trilogy.arch.adapter.jira.JiraApi$JiraApiException: OOPS!\nCause: java.lang.RuntimeException: Details\n"));
        assertThat(
                out.toString(),
                equalTo(
                        "Not re-creating stories:\n  - story that should not be created\n\n" +
                                "Checking epic...\n\n" +
                                "Attempting to create stories...\n\n"));
        assertThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldHandleNoStoriesToCreate() throws Exception {
        mockGitInterface();

        final var command = "au publish -b master -u user -p password " + rootDir.getAbsolutePath() + "/architecture-updates/no-stories-to-create/ " + rootDir.getAbsolutePath();
        final int statusCode = execute(app, command);
        verifyNoMoreInteractions(mockedJiraApi);

        collector.checkThat(err.toString(), equalTo("ERROR: No stories to create.\n"));
        collector.checkThat(out.toString(), equalTo("Not re-creating stories:\n  - story that should not be created\n\n"));
        collector.checkThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldDisplayGetStoryErrorsFromJira() throws Exception {
        final var epic = new Jira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");

        mockGitInterface();
        when(mockedJiraApi.getStory(epic))
                .thenThrow(new JiraApiException("OOPS!", null));

        final String command = command();
        final int statusCode = execute(app, command);

        assertThat(err.toString(), equalTo("Jira API failed\nError: net.trilogy.arch.adapter.jira.JiraApi$JiraApiException: OOPS!\n"));
        assertThat(out.toString(), equalTo("Not re-creating stories:\n  - story that should not be created\n\nChecking epic...\n\n"));
        assertThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldGracefullyHandleErrorsInGitInterface() throws Exception {
        when(mockedGitInterface.load(any(), any()))
                .thenThrow(new RuntimeException("Boom!"));

        final String command = command();
        final int status = execute(app, command);

        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), equalTo("Unable to load product architecture in branch: master\nError: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldGracefullyHandleAuUpdateWriteFailure() throws Exception {
        // GIVEN:
        Jira epic = Jira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult(1L, "PROJ_KEY");
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        when(mockedJiraApi.createStories(anyList(), anyString(), anyLong()))
                .thenReturn(List.of(
                        succeeded("ABC-123", "link-to-ABC-123"),
                        succeeded("ABC-223", "link-to-ABC-223")));
        mockGitInterface();
        doThrow(new RuntimeException("ERROR", new RuntimeException("Boom!"))).when(spiedFilesFacade).writeString(any(), any());

        // WHEN:
        final String command = "au publish -b master -u user -p password " + testCloneDirectory + " " + rootDir.getAbsolutePath();
        final int status = execute(app, command);

        // THEN:
        collector.checkThat(
                err.toString(),
                equalTo("Unable to write update to AU.\nError: java.lang.RuntimeException: ERROR\nCause: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, not(equalTo(0)));
    }

    private void mockGitInterface() throws IOException, GitAPIException, GitInterface.BranchNotFoundException {
        when(mockedGitInterface.load("master", rootDir.toPath().resolve("product-architecture.yml"))).thenReturn(
                YAML_OBJECT_MAPPER.readValue(
                        Files.readString(
                                rootDir.toPath().resolve("product-architecture.yml"))
                                .replaceAll("34", "DELETED-COMPONENT-ID"),
                        ArchitectureDataStructure.class));
    }

    private String command() {
        return format(
                "au publish -b master -u user -p password %s %s",
                testCloneDirectory,
                rootDir.getAbsolutePath());
    }

    private static List<JiraStory> getExpectedJiraStoriesToCreate() {
        return List.of(
                new JiraStory(
                        "story that should be created",
                        List.of(
                                new JiraTdd(
                                        new TddId("[SAMPLE-TDD-ID]"),
                                        new Tdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/Reset Password Controller",
                                        null),
                                new JiraTdd(
                                        new TddId("[SAMPLE-TDD-ID-2]"),
                                        new Tdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/E-mail Component",
                                        null)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new FunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("[SAMPLE-TDD-ID]")))))),
                new JiraStory(
                        "story that failed to be created",
                        singletonList(new JiraTdd(
                                new TddId("[SAMPLE-TDD-ID]"),
                                new Tdd("[SAMPLE TDD TEXT]", null),
                                "c4://Internet Banking System/API Application/Reset Password Controller",
                                null)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new FunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("[SAMPLE-TDD-ID]")))))));
    }

    private static List<JiraStory> getExpectedJiraStoriesWithTddContentToCreate() {
        final var tddContent = new TddContent("## TDD Content for Typical Component\n**TDD 1.0**\n", "TDD 1.0 : Component-29.md");
        final var tdd = new Tdd(null, "TDD 1.0 : Component-29.md").withContent(tddContent);

        return asList(new JiraStory(
                        "story that should be created",
                        singletonList(new JiraTdd(
                                new TddId("TDD 1.0"),
                                tdd,
                                "c4://Internet Banking System/API Application/Sign In Controller",
                                tddContent)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new FunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("TDD 1.0")))))),
                new JiraStory("story that should be created for no pr",
                        singletonList(new JiraTdd(
                                TddId.noPr(),
                                null,
                                "c4://Internet Banking System/API Application/Sign In Controller",
                                null)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new FunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("TDD 1.0")))))));
    }
}

