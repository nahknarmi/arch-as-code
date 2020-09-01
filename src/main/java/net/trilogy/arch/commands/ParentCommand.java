package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

@Command(
        name = "arch-as-code",
        description = "Architecture as code",
        mixinStandardHelpOptions = true,
        versionProvider = ParentCommand.VersionProvider.class
)
public class ParentCommand implements Callable<Integer>, DisplaysOutputMixin {
    public static final String PRODUCT_ARCHITECTURE_FILE_NAME = "product-architecture.yml";

    @Getter
    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        logArgs();
        print(spec.commandLine().getUsageMessage());
        return 0;
    }

    static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() throws IOException {
            final var properties = new Properties();
            properties.load(getClass().getResourceAsStream("/git.properties"));
            return new String[]{format("arch-as-code version %s, build %s",
                    properties.getProperty("git.build.version"),
                    properties.getProperty("git.commit.id.abbrev"))};
        }
    }
}
