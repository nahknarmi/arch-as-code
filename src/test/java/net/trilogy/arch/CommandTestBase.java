package net.trilogy.arch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ErrorCollector;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public abstract class CommandTestBase {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    @Rule
    public final SystemOutRule dummyOut = new SystemOutRule().enableLog().mute();
    @Rule
    public final SystemErrRule dummyErr = new SystemErrRule().enableLog().mute();
}
