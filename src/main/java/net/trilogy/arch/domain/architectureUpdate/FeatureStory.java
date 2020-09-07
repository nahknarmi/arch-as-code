package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.FunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;

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
    private final List<TddId> tddReferences;
    @JsonProperty(value = "functional-requirement-references")
    private final List<FunctionalRequirementId> requirementReferences;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public FeatureStory(
            @JsonProperty("title") String title,
            @JsonProperty("jira") Jira jira,
            @JsonProperty("tdd-references") List<TddId> tddReferences,
            @JsonProperty("functional-requirement-references") List<FunctionalRequirementId> requirementReferences
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
                List.of(TddId.blank()),
                List.of(FunctionalRequirementId.blank())
        );
    }
}
