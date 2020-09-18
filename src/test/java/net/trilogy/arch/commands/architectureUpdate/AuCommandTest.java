package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.CommandTestBase;
import org.junit.Test;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class AuCommandTest extends CommandTestBase {
    @Test
    public void rootCommandShouldPrintUsage() {
        collector.checkThat(
                execute("au"),
                equalTo(0));
        collector.checkThat(
                dummyOut.getLog(),
                containsString("Usage:"));
    }
}
