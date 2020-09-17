package net.trilogy.arch.commands.architectureUpdate;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class AuCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void rootCommandShouldPrintUsage() {
        collector.checkThat(
                execute("au"),
                equalTo(0)
        );

        collector.checkThat(
                out.toString(),
                containsString("Usage:")
        );
    }
}
