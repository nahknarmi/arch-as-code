package net.nahknarmi.arch;

import net.nahknarmi.arch.commands.Arc;
import net.nahknarmi.arch.commands.PublishCommand;
import picocli.CommandLine;

public class Bootstrap {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Arc())
                .addSubcommand(new PublishCommand())
                .execute(args);
        System.exit(exitCode);
    }
}
