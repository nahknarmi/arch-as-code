package net.trilogy.arch;

import lombok.Builder;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory;
import net.trilogy.arch.adapter.graphviz.GraphvizInterface;
import net.trilogy.arch.adapter.jira.JiraApiFactory;
import net.trilogy.arch.adapter.structurizr.StructurizrAdapter;
import net.trilogy.arch.commands.*;
import net.trilogy.arch.commands.architectureUpdate.*;
import net.trilogy.arch.config.AppConfig;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine;

@Builder
public class Application {

    @Builder.Default
    private final AppConfig appConfig = AppConfig.builder().build();

    @Builder.Default
    private final StructurizrAdapter structurizrAdapter = new StructurizrAdapter();
    @Builder.Default
    private final GoogleDocsAuthorizedApiFactory googleDocsAuthorizedApiFactory = new GoogleDocsAuthorizedApiFactory();
    @Builder.Default
    private final JiraApiFactory jiraApiFactory = new JiraApiFactory();
    @Builder.Default
    private final FilesFacade filesFacade = new FilesFacade();
    @Builder.Default
    private final GitInterface gitInterface = new GitInterface();
    @Builder.Default
    private final GraphvizInterface graphvizInterface = new GraphvizInterface();

    private CommandLine getCli() {
        return new CommandLine(new ParentCommand())
                .addSubcommand(new InitializeCommand(filesFacade))
                .addSubcommand(new ValidateCommand())
                .addSubcommand(new PublishCommand(structurizrAdapter))
                .addSubcommand(new ImportCommand(filesFacade))
                .addSubcommand(new ListComponentsCommand(filesFacade))
                .addSubcommand(new DiffCommand(filesFacade, gitInterface, graphvizInterface, new ArchitectureUpdateReader(filesFacade)))
                .addSubcommand(
                        new CommandLine(new AuCommand())
                                .addSubcommand(new AuInitializeCommand(filesFacade))
                                .addSubcommand(new AuNewCommand(googleDocsAuthorizedApiFactory, filesFacade, gitInterface))
                                .addSubcommand(new AuValidateCommand(filesFacade, gitInterface))
                                .addSubcommand(new AuPublishStoriesCommand(jiraApiFactory, filesFacade, gitInterface))
                                .addSubcommand(new AuAnnotateCommand(filesFacade))
                );
    }

    public static void main(String[] args) {
        final var app = Application.builder().build();

        int exitCode = app.execute(args);

        if (exitCode != 0) {
            app.getCli().getCommandSpec().commandLine().getOut().println("Command failed, for more info please check log file at: " + System.getProperty("user.home") +
                    "/.arch-as-code/arch-as-code.log");
        }
        System.exit(exitCode);
    }

    public int execute(String[] args) {
        return getCli().execute(args);
    }

}
