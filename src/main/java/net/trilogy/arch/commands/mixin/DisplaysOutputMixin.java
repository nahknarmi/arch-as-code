package net.trilogy.arch.commands.mixin;

import org.apache.logging.log4j.LogManager;
import picocli.CommandLine;

import java.util.stream.Collectors;

public interface DisplaysOutputMixin {
    CommandLine.Model.CommandSpec getSpec();

    default void print(String message) {
        getSpec().commandLine().getOut().println(message);

        LogManager.getLogger(getClass()).info(message);
    }

    default void logArgs() {
        LogManager.getLogger(getClass()).info(getClass().getSimpleName() + " with args : " + getSpec().args().stream().map(this::maskedParamValue
        ).collect( Collectors.joining( " ") ));
    }

    default String maskedParamValue(CommandLine.Model.ArgSpec spec) {
        String label = spec.paramLabel().toLowerCase();
        if (label.contains("secret") || label.contains("password")) {
            return spec.paramLabel() + ":******";
        }
        return spec.paramLabel() + ":" + spec.getValue();
    }
}
