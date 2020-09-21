package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ToString
@Getter
@EqualsAndHashCode
public class Jira {
    @JsonProperty(value = "ticket")
    @NonNull
    private final String ticket;
    // TODO: Should this be a JDK URI?
    @JsonProperty(value = "link")
    @NonNull
    private final String link;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public Jira(
            @JsonProperty("ticket") String ticket,
            @JsonProperty("link") String link) {
        this.ticket = ticket;
        this.link = link;
    }

    public static Jira blank() {
        return new Jira("[SAMPLE JIRA TICKET]", "[SAMPLE JIRA TICKET LINK]");
    }
}
