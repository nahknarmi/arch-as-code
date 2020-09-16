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
public class MilestoneDependency {
    @JsonProperty(value = "description")
    private final String description;
    @JsonProperty(value = "links")
    private final List<Link> links;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public MilestoneDependency(
            @JsonProperty("description") String description,
            @JsonProperty("links") List<Link> links) {
        this.description = description;
        this.links = links;
    }

    public static MilestoneDependency blank() {
        return new MilestoneDependency("[SAMPLE MILESTONE DEPENDENCY]", List.of(Link.blank()));
    }
}
