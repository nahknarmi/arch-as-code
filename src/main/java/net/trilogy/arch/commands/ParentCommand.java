package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

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
            URL url = getClass().getResource("/version.txt");
            checkNotNull(url, "Failed to retrieve version information.");
            Properties properties = new Properties();
            properties.load(url.openStream());
            return new String[]{"arch-as-code version " + properties.getProperty("Version")};
        }
    }
}
