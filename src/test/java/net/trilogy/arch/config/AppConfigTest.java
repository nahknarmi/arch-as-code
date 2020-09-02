package net.trilogy.arch.config;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AppConfigTest {

    @Test
    public void shouldGetDefaultLogPath() {
        String logPath = AppConfig.builder().build().getLogPath();
        assertTrue(logPath.endsWith("/.arch-as-code/arch-as-code.log"));
    }
}
