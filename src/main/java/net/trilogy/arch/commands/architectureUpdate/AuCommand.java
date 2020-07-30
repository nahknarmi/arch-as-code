package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(
        name = "architecture-update",
        aliases = "au",
        description = "Namespace for Architecture Update commands.",
        mixinStandardHelpOptions=true
)
public class AuCommand implements Callable<Integer>, DisplaysOutputMixin {
    public static final String ARCHITECTURE_UPDATES_ROOT_FOLDER = "architecture-updates";
    public static final String ARCHITECTURE_UPDATE_FILE_NAME = "architecture-update.yml";

    @Getter
    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        logArgs();
        print(spec.commandLine().getUsageMessage());
        return 0;
    }
}
