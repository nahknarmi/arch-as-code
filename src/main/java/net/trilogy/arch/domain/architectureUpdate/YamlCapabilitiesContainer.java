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
@ToString
@EqualsAndHashCode
public class YamlCapabilitiesContainer {
    @JsonProperty(value = "epic")
    private final YamlEpic epic;
    @JsonProperty(value = "feature-stories")
    private final List<YamlFeatureStory> featureStories;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlCapabilitiesContainer(
            @JsonProperty("epic") YamlEpic epic,
            @JsonProperty("feature-stories") List<YamlFeatureStory> featureStories
    ) {
        this.epic = epic;
        this.featureStories = featureStories;
    }

    public static YamlCapabilitiesContainer blank() {
        return new YamlCapabilitiesContainer(YamlEpic.blank(), List.of(YamlFeatureStory.blank()));
    }
}
