package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@EqualsAndHashCode
@ToString
public class Link {
    @JsonProperty(value = "description")
    private final String description;
    // TODO: Should this be a JDK URI?
    @JsonProperty(value = "link")
    private final String link;

    @JsonCreator(mode = PROPERTIES)
    public Link(
            @JsonProperty("description") String description,
            @JsonProperty("link") String link) {
        this.description = description;
        this.link = link;
    }

    public static Link blank() {
        return new Link("[SAMPLE LINK DESCRIPTION]", "[SAMPLE-LINK]");
    }
}
