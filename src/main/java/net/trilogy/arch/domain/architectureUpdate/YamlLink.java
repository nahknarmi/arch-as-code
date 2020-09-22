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
public class YamlLink {
    @JsonProperty(value = "description")
    private final String description;
    // TODO: Should this be a JDK URI?
    @JsonProperty(value = "link")
    private final String link;

    @JsonCreator(mode = PROPERTIES)
    public YamlLink(
            @JsonProperty("description") String description,
            @JsonProperty("link") String link) {
        this.description = description;
        this.link = link;
    }

    public static YamlLink blank() {
        return new YamlLink("[SAMPLE LINK DESCRIPTION]", "[SAMPLE-LINK]");
    }
}
