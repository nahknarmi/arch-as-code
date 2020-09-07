package net.trilogy.arch.adapter.jira;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class JiraCreateStoryStatus {
    private final String issueKey;
    private final String issueLink;
    private final String error;

    public static JiraCreateStoryStatus failed(String error) {
        return new JiraCreateStoryStatus(null, null, error);
    }

    public static JiraCreateStoryStatus succeeded(String issueKey, String issueLink) {
        return new JiraCreateStoryStatus(issueKey, issueLink, null);
    }

    public boolean isSuccess() {
        return null == error;
    }
}
