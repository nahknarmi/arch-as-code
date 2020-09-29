package net.trilogy.arch.adapter.jira;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@ToString
public class JiraRemoteStoryStatus {
    private final String issueKey;
    private final String issueLink;
    private final String error;
    private final JiraIssueConvertible issue;

    public static JiraRemoteStoryStatus failed(String error, JiraIssueConvertible issue) {
        return new JiraRemoteStoryStatus(null, null, error, issue);
    }

    public static JiraRemoteStoryStatus succeeded(String issueKey, String issueLink, JiraIssueConvertible issue) {
        return new JiraRemoteStoryStatus(issueKey, issueLink, null, issue);
    }

    public boolean isSuccess() {
        return null == error;
    }
}
