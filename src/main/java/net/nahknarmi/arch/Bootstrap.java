package net.nahknarmi.arch;

import net.nahknarmi.arch.commands.ParentCommand;
import net.nahknarmi.arch.commands.PublishCommand;
import picocli.CommandLine;

public class Bootstrap {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ParentCommand())
                .addSubcommand(new PublishCommand())
                .execute(args);
        System.exit(exitCode);
    }
}
