package net.nahknarmi.arch;

import net.nahknarmi.arch.publish.ArchitectureDataStructurePublisher;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

public class Bootstrap {

    public static void main(String[] args) {
        new CommandLine(new Cli()).execute(args);
    }

    @CommandLine.Command(name = "arc", description = "Architecture as code")
    static class Cli implements Callable<Integer> {
        @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_ROOT")
        private File productDocumentationRoot;

        @CommandLine.Parameters(index = "1", paramLabel = "PRODUCT_NAME")
        private String productName;

        @Override
        public Integer call() throws Exception {
            ArchitectureDataStructurePublisher.create(productDocumentationRoot).publish(productName);
            return 0;
        }
    }
}
