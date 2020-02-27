package net.nahknarmi.arch.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

@CommandLine.Command(name = "arch-as-code", description = "Architecture as code", mixinStandardHelpOptions = true, versionProvider = ParentCommand.VersionProvider.class)
public class ParentCommand implements Callable<Integer> {
    private static final Log logger = LogFactory.getLog(ParentCommand.class);

    @Override
    public Integer call() {
        logger.info("Arch as code");
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
