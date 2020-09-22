package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@EqualsAndHashCode
@ToString
public class YamlMilestoneDependency {
    @JsonProperty(value = "description")
    private final String description;
    @JsonProperty(value = "links")
    private final List<YamlLink> links;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlMilestoneDependency(
            @JsonProperty("description") String description,
            @JsonProperty("links") List<YamlLink> links) {
        this.description = description;
        this.links = links;
    }

    public static YamlMilestoneDependency blank() {
        return new YamlMilestoneDependency("[SAMPLE MILESTONE DEPENDENCY]", List.of(YamlLink.blank()));
    }
}
