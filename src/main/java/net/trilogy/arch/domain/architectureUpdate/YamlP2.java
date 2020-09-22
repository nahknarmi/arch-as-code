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
public class YamlP2 {
    // TODO: Should be a JDK URI, not a string
    @JsonProperty(value = "link")
    private final String link;
    @JsonProperty(value = "jira")
    private final YamlJira jira;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlP2(
            @JsonProperty("link") String link,
            @JsonProperty("jira") YamlJira jira) {
        this.link = link;
        this.jira = jira;
    }

    public static YamlP2 blank() {
        return new YamlP2("[SAMPLE LINK TO P2]", YamlJira.blank());
    }
}
