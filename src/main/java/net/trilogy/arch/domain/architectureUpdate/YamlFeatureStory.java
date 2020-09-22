package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ToString
@Getter
@EqualsAndHashCode
public class YamlFeatureStory {
    @JsonProperty(value = "title")
    private final String title;
    @JsonProperty(value = "jira")
    private final YamlJira jira;
    @JsonProperty(value = "tdd-references")
    private final List<TddId> tddReferences;
    @JsonProperty(value = "functional-requirement-references")
    private final List<FunctionalRequirementId> requirementReferences;
    @JsonProperty(value = "e2e")
    private final YamlE2E e2e;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlFeatureStory(
            @JsonProperty("title") String title,
            @JsonProperty("jira") YamlJira jira,
            @JsonProperty("tdd-references") List<TddId> tddReferences,
            @JsonProperty("functional-requirement-references") List<FunctionalRequirementId> requirementReferences,
            @JsonProperty("e2e") YamlE2E e2e) {
        this.title = title;
        this.jira = jira;
        this.tddReferences = tddReferences;
        this.requirementReferences = requirementReferences;
        this.e2e = e2e;
    }

    public boolean exists() {
        if (null == jira) return false;
        if (null == jira.getTicket()) return false;
        if (null == jira.getLink() && jira.getTicket().isBlank())
            return false;
        if (jira.getLink().isBlank()) return false;

        return true;
    }

    public static YamlFeatureStory blank() {
        return new YamlFeatureStory(
                "[SAMPLE FEATURE STORY TITLE]",
                new YamlJira("", ""),
                List.of(TddId.blank()),
                List.of(FunctionalRequirementId.blank()),
                YamlE2E.blank());
    }
}
