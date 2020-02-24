package net.nahknarmi.arch.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "arch-as-code", description = "Architecture as code", mixinStandardHelpOptions = true, version = "1.0.0")
public class ParentCommand implements Callable<Integer> {
    private static final Log logger = LogFactory.getLog(ParentCommand.class);

    @Override
    public Integer call() {
        logger.info("Arch as code");
        return 0;
    }
}
