package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.jira.JiraApiFactory;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine.Command;

@Command(name = "finalizeAndPublish", description = "Annotate AU, validate AU and publish AU Jira stories.", mixinStandardHelpOptions = true)
public class AuFinalizeAndPublishCommand extends AuPublishStoriesCommand {

    public AuFinalizeAndPublishCommand(JiraApiFactory jiraApiFactory, FilesFacade filesFacade, GitInterface gitInterface) {
        super(jiraApiFactory, filesFacade, gitInterface);
    }

    public Integer call() {
        AuAnnotateCommand auAnnotateCommand = createAuAnnotateCommand();
        Integer annotateResults = auAnnotateCommand.call();
        if (annotateResults != 0 ) {
            return annotateResults;
        }

        Integer validateResults = createAuValidateCommand().call();
        if (validateResults != 0) {
            return validateResults;
        }
        return publishToJira();
    }

    Integer publishToJira() {
        return super.call();
    }

    AuValidateCommand createAuValidateCommand() {
        return new AuValidateCommand(
                getFilesFacade(),
                getGitInterface(),
                getSpec(),
                getArchitectureUpdateDirectory(),
                getProductArchitectureDirectory(),
                getBaseBranch());
    }

    AuAnnotateCommand createAuAnnotateCommand() {
        return new AuAnnotateCommand(
                getFilesFacade(),
                getSpec(),
                getArchitectureUpdateDirectory(),
                getProductArchitectureDirectory());
    }

}

