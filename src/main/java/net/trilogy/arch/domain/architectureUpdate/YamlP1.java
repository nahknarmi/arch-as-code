package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@EqualsAndHashCode
@ToString
public class YamlP1 {
    // TODO: Should be a JDK URI, not a string
    @JsonProperty(value = "link")
    private final String link;
    @JsonProperty(value = "jira")
    private final YamlJira jira;
    @JsonProperty(value = "executive-summary")
    private final String executiveSummary;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlP1(
            @JsonProperty("link") String link,
            @JsonProperty("jira") YamlJira jira,
            @JsonProperty("executive-summary") String executiveSummary) {
        this.link = link;
        this.jira = jira;
        this.executiveSummary = executiveSummary;
    }

    public static YamlP1 blank() {
        return new YamlP1("[SAMPLE LINK TO P1]", YamlJira.blank(), "[SAMPLE EXECUTIVE SUMMARY]");
    }
}
