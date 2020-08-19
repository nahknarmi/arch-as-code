package net.trilogy.arch.commands.mixin;

import org.apache.logging.log4j.LogManager;
import picocli.CommandLine;

public interface DisplaysErrorMixin {

    CommandLine.Model.CommandSpec getSpec();

    default void printError(String errorMessage) {
        getSpec().commandLine().getErr().println(errorMessage);
    }

    default void printError(String errorMessage, Exception exception) {
        printError(errorMessage);
        printError("Error: " + exception);
        if(exception.getCause() != null) {
            printError("Cause: " + exception.getCause());
        }

        LogManager.getLogger(getClass()).error(errorMessage, exception);
    }
}
