package net.trilogy.arch;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ErrorCollector;

public abstract class CommandTestBase {
    /**
     * When testing STDOUT and STDERR, toggle this.  The default should be
     * {@code false}, so that passing tests do not dump STDOUT/STDERR to the
     * console; setting it to {@code true} should still pass tests, but also
     * print to the console at tests' conclusion.
     */
    public static final boolean DEBUG = false;

    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    @Rule
    public final SystemOutRule dummyOut = DEBUG
            ? new SystemOutRule().enableLog()
            : new SystemOutRule().enableLog().mute();
    @Rule
    public final SystemErrRule dummyErr = DEBUG
            ? new SystemErrRule().enableLog()
            : new SystemErrRule().enableLog().mute();
}
