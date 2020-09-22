package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.jira.JiraApi;
import net.trilogy.arch.adapter.jira.JiraApiException;
import net.trilogy.arch.adapter.jira.JiraStory.InvalidStoryException;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureFromGitMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.services.architectureUpdate.StoryPublishingService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.adapter.jira.JiraApiFactory.newJiraApi;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.ARCHITECTURE_UPDATE_YML;

@Command(name = "publish", description = "Publish stories.", mixinStandardHelpOptions = true)
public class AuPublishStoriesCommand implements Callable<Integer>, LoadArchitectureMixin, LoadArchitectureFromGitMixin, DisplaysOutputMixin {
    private final ArchitectureUpdateReader architectureUpdateReader;

    @Getter
    private final FilesFacade filesFacade;
    @Getter
    private final GitInterface gitInterface;
    @Getter
    @Option(names = {"-b", "--branch-of-base-architecture"}, description = "Name of git branch from which this AU was branched. Used to get names of components. Usually 'master'. Also can be a commit or tag.", required = true)
    String baseBranch;
    @Getter
    @Parameters(index = "0", description = "Directory name of architecture update to validate")
    private File architectureUpdateDirectory;
    @Getter
    @Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;
    @Option(names = {"-u", "--jira-username"}, description = "Jira username", required = true)
    private String username;
    @Option(names = {"-p", "--jira-password"}, description = "Jira password", arity = "0..1", interactive = true, required = true)
    private char[] password;

    @Getter
    @Spec
    private CommandSpec spec;

    public AuPublishStoriesCommand(FilesFacade filesFacade, GitInterface gitInterface) {
        this.filesFacade = filesFacade;
        this.gitInterface = gitInterface;

        architectureUpdateReader = new ArchitectureUpdateReader(filesFacade);
    }

    @Override
    public Integer call() {
        logArgs();
        final Path auPath = architectureUpdateDirectory.toPath();

        var au = loadAu(auPath);
        if (au.isEmpty()) return 1;

        final var beforeAuArchitecture = loadArchitectureFromGitOrPrintError(baseBranch, "Unable to load product architecture in branch: " + baseBranch);
        if (beforeAuArchitecture.isEmpty()) return 1;

        final var afterAuArchitecture = loadArchitectureOrPrintError("Unable to load architecture.");
        if (afterAuArchitecture.isEmpty()) return 1;

        var jiraApi = getJiraApi();
        if (jiraApi.isEmpty()) return 1;

        final StoryPublishingService jiraService = new StoryPublishingService(
                spec.commandLine().getOut(),
                spec.commandLine().getErr(),
                jiraApi.get());

        var updatedAu = createStories(
                au.get(),
                beforeAuArchitecture.get(),
                afterAuArchitecture.get(),
                jiraService);
        if (updatedAu.isEmpty()) return 1;

        try {
            filesFacade.writeString(
                    auPath.resolve(ARCHITECTURE_UPDATE_YML),
                    YAML_OBJECT_MAPPER.writeValueAsString(updatedAu.get()));
        } catch (Exception e) {
            printError("Unable to write update to AU.", e);
            return 1;
        }

        return 0;
    }

    private Optional<ArchitectureUpdate> createStories(
            ArchitectureUpdate au,
            ArchitectureDataStructure beforeAuArchitecture,
            ArchitectureDataStructure afterAuArchitecture,
            StoryPublishingService jiraService) {
        try {
            return Optional.of(jiraService.createOrUpdateStories(au, beforeAuArchitecture, afterAuArchitecture));
        } catch (JiraApiException e) {
            printError("Jira API failed", e);
        } catch (InvalidStoryException e) {
            printError("ERROR: Some stories are invalid. Please run 'au validate' command.");
        }

        return Optional.empty();
    }

    private Optional<JiraApi> getJiraApi() {
        try {
            return Optional.of(newJiraApi(filesFacade, productArchitectureDirectory.toPath(), username, password));
        } catch (Exception e) {
            printError("Unable to load JIRA configuration.", e);
            return Optional.empty();
        }
    }

    private Optional<ArchitectureUpdate> loadAu(Path auPath) {
        try {
            return Optional.of(architectureUpdateReader.loadArchitectureUpdate(auPath));
        } catch (Exception e) {
            printError("Unable to load architecture update.", e);
            return Optional.empty();
        }
    }
}

