package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class CapabilitiesContainer {
    @JsonProperty(value = "epic")
    private final Epic epic;
    @JsonProperty(value = "feature-stories")
    private final List<FeatureStory> featureStories;

    @Builder(toBuilder = true)
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CapabilitiesContainer(
            @JsonProperty("epic") Epic epic,
            @JsonProperty("feature-stories") List<FeatureStory> featureStories
    ) {
        this.epic = epic;
        this.featureStories = featureStories;
    }

    public static CapabilitiesContainer blank() {
        return new CapabilitiesContainer(Epic.blank(), List.of(FeatureStory.blank()));
    }
}
