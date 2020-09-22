package net.trilogy.arch.adapter.jira;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class JiraRemoteStoryStatus {
    private final String issueKey;
    private final String issueLink;
    private final String error;

    public static JiraRemoteStoryStatus failed(String error) {
        return new JiraRemoteStoryStatus(null, null, error);
    }

    public static JiraRemoteStoryStatus succeeded(String issueKey, String issueLink) {
        return new JiraRemoteStoryStatus(issueKey, issueLink, null);
    }

    public boolean isSuccess() {
        return null == error;
    }
}
