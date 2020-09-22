package net.trilogy.arch.adapter.jira;

import lombok.NonNull;

public class JiraApiException extends Exception {
    public JiraApiException(@NonNull String message, Throwable cause) {
        super(message, cause);
    }
}
