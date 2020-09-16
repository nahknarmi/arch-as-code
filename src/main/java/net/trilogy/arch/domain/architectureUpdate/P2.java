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
public class P2 {
    // TODO: Should be a JDK URI, not a string
    @JsonProperty(value = "link")
    private final String link;
    @JsonProperty(value = "jira")
    private final Jira jira;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public P2(
            @JsonProperty("link") String link,
            @JsonProperty("jira") Jira jira) {
        this.link = link;
        this.jira = jira;
    }

    public static P2 blank() {
        return new P2("[SAMPLE LINK TO P2]", Jira.blank());
    }
}
