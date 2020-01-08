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
        @CommandLine.Parameters(index = "0", paramLabel = "PRODUCT_DOCUMENTATION_PATH", description = "Product documentation root where data-structure.yml is located.")
        private File productDocumentationRoot;

        @Override
        public Integer call() throws Exception {
            ArchitectureDataStructurePublisher.create(productDocumentationRoot).publish();
            return 0;
        }
    }
}
