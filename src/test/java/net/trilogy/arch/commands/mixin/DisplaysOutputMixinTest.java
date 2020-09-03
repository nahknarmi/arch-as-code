package net.trilogy.arch.commands.mixin;

import org.junit.Test;
import picocli.CommandLine;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DisplaysOutputMixinTest {
    @Test
    public void shouldMaskSecretValue() {
        CommandLine.Model.ArgSpec argSpec = mock(CommandLine.Model.ArgSpec.class);

        when(argSpec.paramLabel()).thenReturn("<apiSecret>");
        when(argSpec.getValue()).thenReturn("pass123");

        var command = new TestCommand();
        assertEquals("<apiSecret>:******", command.maskedParamValue(argSpec));
    }

    @Test
    public void shouldMaskPasswordValue() {
        CommandLine.Model.ArgSpec argSpec = mock(CommandLine.Model.ArgSpec.class);

        when(argSpec.paramLabel()).thenReturn("<apiPassword>");
        when(argSpec.getValue()).thenReturn("pass123");

        var command = new TestCommand();
        assertEquals("<apiPassword>:******", command.maskedParamValue(argSpec));
    }

    @Test
    public void shouldNotMaskRegularValues() {
        CommandLine.Model.ArgSpec argSpec = mock(CommandLine.Model.ArgSpec.class);

        when(argSpec.paramLabel()).thenReturn("<apiUser>");
        when(argSpec.getValue()).thenReturn("user123");

        var command = new TestCommand();
        assertEquals("<apiUser>:user123", command.maskedParamValue(argSpec));
    }

    static class TestCommand implements DisplaysOutputMixin {
        @Override
        public CommandLine.Model.CommandSpec getSpec() {
            return null;
        }
    }
}
