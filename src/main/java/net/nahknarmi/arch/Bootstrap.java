package net.nahknarmi.arch;

import net.nahknarmi.arch.commands.InitializeCommand;
import net.nahknarmi.arch.commands.ParentCommand;
import net.nahknarmi.arch.commands.PublishCommand;
import net.nahknarmi.arch.commands.ValidateCommand;
import picocli.CommandLine;

public class Bootstrap {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ParentCommand())
                .addSubcommand(new InitializeCommand())
                .addSubcommand(new ValidateCommand())
                .addSubcommand(new PublishCommand())
                .execute(args);
        System.exit(exitCode);
    }
}
