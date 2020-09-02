package net.trilogy.arch.config;

import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.Optional;

@Builder
public class AppConfig {
    @Getter
    @Builder.Default
    private final String logPath = envOrDefault("LOG_PATH", System.getProperty("user.home") + File.separator + ".arch-as-code" + File.separator + "arch-as-code.log");

    private static String envOrDefault(String envName, String orElse) {
        return Optional.ofNullable(System.getenv(envName)).orElse(orElse);
    }
}
