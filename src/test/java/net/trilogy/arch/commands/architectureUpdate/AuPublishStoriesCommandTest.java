package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.Application;
import net.trilogy.arch.CommandTestBase;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory;
import net.trilogy.arch.adapter.jira.*;
import net.trilogy.arch.adapter.jira.JiraStory.JiraFunctionalRequirement;
import net.trilogy.arch.adapter.jira.JiraStory.JiraTdd;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.*;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalArea.FunctionalAreaId;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.facade.FilesFacade;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.trilogy.arch.TestHelper.ROOT_PATH_TO_TEST_AU_PUBLISH;
import static net.trilogy.arch.TestHelper.execute;
import static net.trilogy.arch.Util.first;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.newJiraApi;
import static net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus.failed;
import static net.trilogy.arch.adapter.jira.JiraRemoteStoryStatus.succeeded;
import static net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate.ARCHITECTURE_UPDATE_YML;
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
import static org.mockito.Mockito.when;

public class AuPublishStoriesCommandTest extends CommandTestBase {
    private File rootDir;
    private Path testCloneDirectory;
    private JiraApi mockedJiraApi;
    private Application app;
    private FilesFacade spiedFiles;
    private GitInterface mockedGitInterface;
    private MockedStatic<JiraApiFactory> jiraApiFactoryMock;

    /** @todo Find better pattern for testing with Mockito.mockStatic */
    private final AtomicBoolean missingJiraConfiguration = new AtomicBoolean(false);

    @Before
    public void setUp() throws Exception {
        mockedJiraApi = mock(JiraApi.class);
        jiraApiFactoryMock = mockStatic(JiraApiFactory.class, invocation -> {
            if (missingJiraConfiguration.get())
                throw new NoSuchFileException("YOUR JIRA CONFIGURATION");
            else return mockedJiraApi;
        });

        spiedFiles = spy(new FilesFacade());

        rootDir = new File(getClass().getResource(ROOT_PATH_TO_TEST_AU_PUBLISH).getPath());

        final var mockedGoogleApiFactory = mock(GoogleDocsAuthorizedApiFactory.class);

        when(newJiraApi(spiedFiles, rootDir.toPath(), "BOB", "NANCY".toCharArray()))
                .thenReturn(mockedJiraApi);

        mockedGitInterface = mock(GitInterface.class);

        app = Application.builder()
                .googleDocsAuthorizedApiFactory(mockedGoogleApiFactory)
                .filesFacade(spiedFiles)
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
        jiraApiFactoryMock.close();

        deleteIfExists(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML));
        deleteIfExists(testCloneDirectory);
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadConfig() throws Exception {
        missingJiraConfiguration.set(true);

        mockGitInterface();
        final var newApp = Application.builder()
                .gitInterface(mockedGitInterface)
                .build();

        // When
        final var status = execute(newApp, genericCommand());

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), containsString(
                "Unable to load JIRA configuration.\n" +
                        "Error: java.nio.file.NoSuchFileException"));
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadAu() throws Exception {
        // Given a filesystem read error
        doThrow(new RuntimeException("ERROR", new RuntimeException("DETAILS")))
                .when(spiedFiles).readString(eq(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML)));

        // When
        final var status = execute(app, genericCommand());

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), equalTo(
                "Unable to load architecture update.\n" +
                        "Error: java.lang.RuntimeException: ERROR\n" +
                        "Cause: java.lang.RuntimeException: DETAILS\n"));
    }

    @Test
    public void shouldFailGracefullyIfFailToLoadArchitecture() throws Exception {
        // Given
        mockGitInterface();
        doThrow(new RuntimeException("ERROR", new RuntimeException("DETAILS")))
                .when(spiedFiles).readString(eq(rootDir.toPath().resolve("product-architecture.yml")));

        // When
        final var status = execute(app, genericCommand());

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), equalTo(
                "Unable to load architecture.\n" +
                        "Error: java.lang.RuntimeException: ERROR\n" +
                        "Cause: java.lang.RuntimeException: DETAILS\n"));
    }

    @Test
    public void shouldFailGracefullyIfUnableToCreateJiraStoryDTO() throws Exception {
        // Given
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(123L, "[SAMPLE JIRA TICKET]", epic.getTicket());
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // When
        final var status = execute(app, specificCommand("invalid-story"));

        // Then
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyErr.getLog(), equalTo(
                "ERROR: Some stories are invalid. Please run 'au validate' command.\n"));
        collector.checkThat(dummyOut.getLog(), equalTo(
                "Not recreating stories:\n" +
                        "  - story that should be updated (already existing jira ticket)\n\n"));

    }

    @Test
    public void shouldQueryJiraForEpic() throws Exception {
        // Given
        final var epic = new YamlJira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");
        mockGitInterface();

        // When
        execute(app, specificCommand("test-clone"));

        // Then
        verify(mockedJiraApi).getStory(epic);
    }

    @Test
    public void shouldTellJiraToCreateStories() throws Exception {
        // GIVEN:
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY", epic.getTicket());
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // WHEN:
        execute(app, genericCommand());

        // THEN:
        final var expected = getExpectedJiraStoriesToCreate();
        verify(mockedJiraApi).createJiraIssues(expected, epicInformation.getIssueKey(), epicInformation.getProjectId());
    }

    @Test
    public void shouldTellJiraToUpdateStories() throws Exception {
        // GIVEN:
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY", epic.getTicket());
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // WHEN:
        execute(app, genericCommand());

        // THEN:
        List<YamlAttribute> attributes = singletonList(new YamlAttribute("Accessible", "User interface functions should be accessible", new YamlJira("AU-77", "https://jira.devfactory.com/browse/AU-77")));
        YamlFunctionalArea functionalArea = new YamlFunctionalArea("Multiple Nodes and Dynamic Routing (SMC, MQ and workbench)s", new YamlJira("AU-76", "https://jira.devfactory.com/browse/AU-76"));
        FunctionalAreaId functionalAreaId = new FunctionalAreaId("AU-76");
        YamlE2E e2e2 = new YamlE2E("Title of E2E 2", "Some useful business goals", functionalAreaId, new YamlJira("existing", "existing link"), attributes);
        final var expected = List.of(new JiraStory(
                new YamlFeatureStory(
                        "story that should be updated",
                        new YamlJira("already existing jira ticket", "link to already existing jira ticket"),
                        singletonList(new TddId("[SAMPLE-TDD-ID]")),
                        singletonList(new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]")),
                        e2e2),
                singletonList(new JiraTdd(
                        new TddId("[SAMPLE-TDD-ID]"),
                        new YamlTdd("[SAMPLE TDD TEXT]", null),
                        "c4://Internet Banking System/API Application/Reset Password Controller",
                        null)),
                singletonList(new JiraFunctionalRequirement(
                        new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                        new YamlFunctionalRequirement(
                                "[SAMPLE REQUIREMENT TEXT]",
                                "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                singletonList(new TddId("[SAMPLE-TDD-ID]")))))),
                new JiraE2E(e2e2, functionalArea));

        verify(mockedJiraApi).updateExistingStories(expected, epic.getTicket());
    }

    @Test
    public void shouldTellJiraToCreateStoriesWithTddContent() throws Exception {
        // GIVEN:
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY", epic.getTicket());
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        mockGitInterface();

        // WHEN:
        execute(app, specificCommand("tdd-content"));

        // THEN:
        verify(mockedJiraApi).createJiraIssues(
                getExpectedJiraStoriesWithTddContentToCreate(), epic.getTicket(), epicInformation.getProjectId());
    }

    @Test
    public void shouldOutputResult() throws Exception {
        // GIVEN:
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(123L, "[SAMPLE JIRA TICKET]", epic.getTicket());
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        YamlArchitectureUpdate au = YamlArchitectureUpdate.blank();
        when(mockedJiraApi.createJiraIssues(anyList(), anyString(), anyLong()))
                .thenReturn(List.of(
                        succeeded("ABC-123", "link-to-ABC-123", new JiraStory(YamlFeatureStory.blank(), au)),
                        succeeded("ABC-223", "link-to-ABC-223", new JiraStory(YamlFeatureStory.blank(), au))));
        mockGitInterface();

        // WHEN:
        execute(app, genericCommand());

        // THEN:
        collector.checkThat(dummyOut.getLog(), equalTo(
                "Not recreating stories:\n" +
                        "  - story that should be updated (already existing jira ticket)\n" +
                        "\n" +
                        "Creating stories in the epic having JIRA key [SAMPLE JIRA TICKET] and project id 123...\n" +
                        "Updating stories in the epic having JIRA key [SAMPLE JIRA TICKET] and project id 123...\n" +
                        "\n" +
                        "Successfully created:\n" +
                        "  - story that should be created\n" +
                        "  - story that failed to be created\n"));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldUpdateAuWithNewJiraTickets() throws Exception {
        // GIVEN:
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(1L, "PROJ_KEY", epic.getTicket());
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        final YamlArchitectureUpdate originalAu = auFromYaml();
        JiraStory jiraStory = new JiraStory(first(originalAu.getCapabilityContainer().getFeatureStories()), originalAu);
        JiraE2E jiraE2E = new JiraE2E(jiraStory.getFeatureStory().getE2e(), first(originalAu.getFunctionalAreas().values()));
        JiraStory failed = new JiraStory(originalAu.getCapabilityContainer().getFeatureStories().get(2), originalAu);
        when(mockedJiraApi.createJiraIssues(anyList(), anyString(), anyLong())).thenReturn(List.of(
                succeeded("ABC-123", "link-to-ABC-123", jiraStory),
                succeeded("ABC-456", "link-to-ABC-456", jiraE2E),
                failed("error-message", failed)));
        mockGitInterface();

        // WHEN:
        execute(app, genericCommand());

        // THEN:
        final YamlArchitectureUpdate actualAu = auFromYaml();

        final var auWithUpdatedStory = originalAu.addJiraToFeatureStory(originalAu,
                jiraStory,
                new YamlJira("ABC-123", "link-to-ABC-123"));

        YamlArchitectureUpdate expectedAu = auWithUpdatedStory.addJiraToFeatureStory(auWithUpdatedStory, jiraE2E, new YamlJira("ABC-456", "link-to-ABC-456"));

        collector.checkThat(actualAu, equalTo(expectedAu));
    }

    @Test
    public void shouldDisplayPartialErrorsWhenCreatingStories() throws Exception {
        // GIVEN:
        final var epic = YamlJira.blank();
        final var epicInformation = new JiraQueryResult(123L, "[SAMPLE JIRA TICKET]", epic.getTicket());
        final YamlArchitectureUpdate au = auFromYaml();
        when(mockedJiraApi.getStory(epic)).thenReturn(epicInformation);
        when(mockedJiraApi.createJiraIssues(anyList(), anyString(), anyLong()))
                .thenReturn(List.of(
                        succeeded("ABC-123", "link-to-ABC-123", new JiraStory(au.getCapabilityContainer().getFeatureStories().get(0), au)),
                        failed("error-message", new JiraStory(au.getCapabilityContainer().getFeatureStories().get(2), au))));
        mockGitInterface();

        // WHEN:
        final var statusCode = execute(app, genericCommand());

        // THEN:
        assertThat(dummyErr.getLog(), equalTo(
                "\n" +
                        "Error! Some stories failed to publish. Please retry. Errors reported by Jira:\n" +
                        "\n" +
                        "Story: \"story that failed to be created\":\n" +
                        "  - error-message\n"));
        assertThat(dummyOut.getLog(), equalTo(
                "Not recreating stories:\n" +
                        "  - story that should be updated (already existing jira ticket)\n" +
                        "\n" +
                        "Creating stories in the epic having JIRA key [SAMPLE JIRA TICKET] and project id 123..." +
                        "\n" +
                        "Updating stories in the epic having JIRA key [SAMPLE JIRA TICKET] and project id 123...\n" +
                        "\n" +
                        "Successfully created:\n" +
                        "  - story that should be created\n"));
        assertThat(statusCode, equalTo(0));
    }

    @Test
    public void shouldDisplayNiceErrorIfCreatingStoriesCrashes() throws Exception {
        when(mockedJiraApi.getStory(any()))
                .thenReturn(new JiraQueryResult(1L, "DEF", "[SAMPLE JIRA TICKET]"));
        when(mockedJiraApi.createJiraIssues(anyList(), anyString(), anyLong()))
                .thenThrow(new JiraApiException(
                        "OOPS!",
                        new RuntimeException("Details")));
        mockGitInterface();

        final var statusCode = execute(app, genericCommand());

        final var log = dummyErr.getLog();
        assertThat(log, containsString(
                "JiraApiException: OOPS!\n" +
                        "Cause: java.lang.RuntimeException: Details\n"));
        // TODO use containsString to make sure the nice message is present, but don't check against specific full output
        assertThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldDisplayGetStoryErrorsFromJira() throws Exception {
        final var epic = new YamlJira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");

        mockGitInterface();
        when(mockedJiraApi.getStory(epic))
                .thenThrow(new JiraApiException("OOPS!", null));

        final var statusCode = execute(app, genericCommand());

        assertThat(dummyErr.getLog(), containsString("JiraApiException: OOPS!\n"));
        assertThat(dummyOut.getLog(), containsString(
                "Not recreating stories:\n" +
                        "  - story that should be updated"));
        assertThat(statusCode, not(equalTo(0)));
    }

    @Test
    public void shouldGracefullyHandleErrorsInGitInterface() throws Exception {
        when(mockedGitInterface.load(any(), any()))
                .thenThrow(new RuntimeException("Boom!"));

        final var status = execute(app, genericCommand());

        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(), equalTo(
                "Unable to load product architecture in branch: master\n" +
                        "Error: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, not(equalTo(0)));
    }

    @Test
    public void shouldGracefullyHandleAuUpdateWriteFailure() throws Exception {
        // GIVEN:
        YamlJira epic = YamlJira.blank();
        final JiraQueryResult epicInformation = new JiraQueryResult(1L, "PROJ_KEY", epic.getTicket());
        when(mockedJiraApi.getStory(epic))
                .thenReturn(epicInformation);
        YamlArchitectureUpdate au = YamlArchitectureUpdate.blank();
        when(mockedJiraApi.createJiraIssues(anyList(), anyString(), anyLong()))
                .thenReturn(List.of(
                        succeeded("ABC-123", "link-to-ABC-123", new JiraStory(YamlFeatureStory.blank(), au)),
                        succeeded("ABC-223", "link-to-ABC-223", new JiraStory(YamlFeatureStory.blank(), au))));
        mockGitInterface();
        doThrow(new RuntimeException("ERROR", new RuntimeException("Boom!"))).when(spiedFiles).writeString(any(), any());

        // WHEN:
        final var status = execute(app, genericCommand());

        // THEN:
        collector.checkThat(dummyErr.getLog(), equalTo(
                "Unable to write update to AU.\n" +
                        "Error: java.lang.RuntimeException: ERROR\n" +
                        "Cause: java.lang.RuntimeException: Boom!\n"));
        collector.checkThat(status, not(equalTo(0)));
    }

    private void mockGitInterface() throws IOException, GitAPIException, GitInterface.BranchNotFoundException {
        when(mockedGitInterface.load("master", rootDir.toPath()
                .resolve("product-architecture.yml")))
                .thenReturn(YAML_OBJECT_MAPPER.readValue(
                        Files.readString(
                                rootDir.toPath().resolve("product-architecture.yml"))
                                .replaceAll("34", "DELETED-COMPONENT-ID"),
                        ArchitectureDataStructure.class));
    }

    private String genericCommand() {
        return format(
                "au publish -b master -u user -p password %s %s",
                testCloneDirectory,
                rootDir.getAbsolutePath());
    }

    private String specificCommand(String variety) {
        return format(
                "au publish -b master -u user -p password %s/architecture-updates/%s/ %s",
                rootDir.getAbsolutePath(),
                variety,
                rootDir.getAbsolutePath());
    }

    private static List<JiraIssueConvertible> getExpectedJiraStoriesToCreate() {
        List<YamlAttribute> attributes = singletonList(new YamlAttribute("Accessible", "User interface functions should be accessible", new YamlJira("AU-77", "https://jira.devfactory.com/browse/AU-77")));
        YamlFunctionalArea functionalArea = new YamlFunctionalArea("Multiple Nodes and Dynamic Routing (SMC, MQ and workbench)s", new YamlJira("AU-76", "https://jira.devfactory.com/browse/AU-76"));
        FunctionalAreaId functionalAreaId = new FunctionalAreaId("AU-76");
        YamlE2E e2e1 = new YamlE2E("Title of E2E 1", "Some useful business goals", functionalAreaId, null, attributes);
        YamlE2E e2e3 = new YamlE2E("Title of E2E 3", "Some useful business goals", functionalAreaId, null, attributes);
        YamlFeatureStory story1 = new YamlFeatureStory(
                "story that should be created",
                new YamlJira("", ""),
                List.of(new TddId("[SAMPLE-TDD-ID]"), new TddId("[SAMPLE-TDD-ID-2]")),
                List.of(new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]")),
                e2e1);
        YamlFeatureStory story3 = new YamlFeatureStory(
                "story that failed to be created",
                new YamlJira("", ""),
                singletonList(new TddId("[SAMPLE-TDD-ID]")),
                singletonList(new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]")),
                e2e3);

        return List.of(
                new JiraStory(
                        story1,
                        List.of(
                                new JiraTdd(
                                        new TddId("[SAMPLE-TDD-ID]"),
                                        new YamlTdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/Reset Password Controller",
                                        null),
                                new JiraTdd(
                                        new TddId("[SAMPLE-TDD-ID-2]"),
                                        new YamlTdd("[SAMPLE TDD TEXT]", null),
                                        "c4://Internet Banking System/API Application/E-mail Component",
                                        null)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new YamlFunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("[SAMPLE-TDD-ID]")))))),
                new JiraStory(
                        story3,
                        singletonList(new JiraTdd(
                                new TddId("[SAMPLE-TDD-ID]"),
                                new YamlTdd("[SAMPLE TDD TEXT]", null),
                                "c4://Internet Banking System/API Application/Reset Password Controller",
                                null)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new YamlFunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("[SAMPLE-TDD-ID]")))))),
                new JiraE2E(e2e1, functionalArea),
                new JiraE2E(e2e3, functionalArea));
    }

    private static List<JiraIssueConvertible> getExpectedJiraStoriesWithTddContentToCreate() {
        final var tddContent = new TddContent(
                "## TDD Content for Typical Component\n" +
                        "**TDD 1.0**\n",
                "TDD 1.0 : Component-29.md");
        final var tdd = new YamlTdd(null, "TDD 1.0 : Component-29.md").withContent(tddContent);

        List<YamlAttribute> attributes = singletonList(new YamlAttribute("Accessible", "User interface functions should be accessible", new YamlJira("AU-77", "https://jira.devfactory.com/browse/AU-77")));
        YamlFunctionalArea functionalArea = new YamlFunctionalArea("Multiple Nodes and Dynamic Routing (SMC, MQ and workbench)s", new YamlJira("AU-76", "https://jira.devfactory.com/browse/AU-76"));
        FunctionalAreaId functionalAreaId = new FunctionalAreaId("AU-76");
        YamlE2E e2e1 = new YamlE2E("Title of E2E 1", "Some useful business goals", functionalAreaId, null, attributes);
        YamlE2E e2e2 = new YamlE2E("Title of E2E 2", "Some useful business goals", functionalAreaId, null, attributes);

        return asList(new JiraStory(
                        new YamlFeatureStory(
                                "story that should be created",
                                new YamlJira("", ""),
                                singletonList(new TddId("TDD 1.0")),
                                singletonList(new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]")),
                                e2e1),
                        singletonList(new JiraTdd(
                                new TddId("TDD 1.0"),
                                tdd,
                                "c4://Internet Banking System/API Application/Sign In Controller",
                                tddContent)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new YamlFunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("TDD 1.0")))))),
                new JiraStory(
                        new YamlFeatureStory(
                                "story that should be created for no pr",
                                new YamlJira("", ""),
                                singletonList(TddId.noPr()),
                                singletonList(new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]")),
                                e2e2),
                        singletonList(new JiraTdd(
                                TddId.noPr(),
                                null,
                                "c4://Internet Banking System/API Application/Sign In Controller",
                                null)),
                        singletonList(new JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new YamlFunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        singletonList(new TddId("TDD 1.0")))))),
                       new JiraE2E(e2e1, functionalArea),
                       new JiraE2E(e2e2, functionalArea));
    }

    private YamlArchitectureUpdate auFromYaml() throws IOException {
        final String actualAuAsString;
        actualAuAsString = Files.readString(testCloneDirectory.resolve(ARCHITECTURE_UPDATE_YML));
        return YAML_OBJECT_MAPPER.readValue(actualAuAsString, YamlArchitectureUpdate.class);
    }
}
