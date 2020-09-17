package net.trilogy.arch.e2e;

import net.trilogy.arch.CommandTestBase;
import org.junit.Test;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ParentCommandE2ETest extends CommandTestBase {
    @Test
    public void rootCommandShouldPrintUsage() {
        collector.checkThat(
                execute(),
                equalTo(0));
        collector.checkThat(
                dummyOut.getLog(),
                containsString("Usage:"));
    }
}
