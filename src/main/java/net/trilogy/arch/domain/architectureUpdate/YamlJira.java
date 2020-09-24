package net.trilogy.arch.domain.architectureUpdate;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ToString
@Getter
@EqualsAndHashCode
public class YamlJira {
    @JsonProperty(value = "ticket")
    @NonNull
    private final String ticket;
    // TODO: Should this be a JDK URI?
    @JsonProperty(value = "link")
    @NonNull
    private final String link;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlJira(
            @JsonProperty("ticket") String ticket,
            @JsonProperty("link") String link) {
        this.ticket = ticket;
        this.link = link;
    }

    public boolean hasJiraKeyAndLink() {
        if (null == getTicket()) return false;
        if (getTicket().isBlank()) return false;
        if (null == getLink()) return false;
        if (getLink().isBlank()) return false;

        return true;
    }

    public static YamlJira blank() {
        return new YamlJira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");
    }


}
