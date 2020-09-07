package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ToString
@Getter
@EqualsAndHashCode
public class FeatureStory {
    @JsonProperty(value = "title")
    private final String title;
    @JsonProperty(value = "jira")
    private final Jira jira;
    @JsonProperty(value = "tdd-references")
    private final List<Tdd.Id> tddReferences;
    @JsonProperty(value = "functional-requirement-references")
    private final List<FunctionalRequirement.Id> requirementReferences;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public FeatureStory(
            @JsonProperty("title") String title,
            @JsonProperty("jira") Jira jira,
            @JsonProperty("tdd-references") List<Tdd.Id> tddReferences,
            @JsonProperty("functional-requirement-references") List<FunctionalRequirement.Id> requirementReferences
    ) {
        this.title = title;
        this.jira = jira;
        this.tddReferences = tddReferences;
        this.requirementReferences = requirementReferences;
    }

    public static FeatureStory blank() {
        return new FeatureStory(
                "[SAMPLE FEATURE STORY TITLE]",
                new Jira("", ""),
                List.of(Tdd.Id.blank()),
                List.of(FunctionalRequirement.Id.blank())
        );
    }
}
