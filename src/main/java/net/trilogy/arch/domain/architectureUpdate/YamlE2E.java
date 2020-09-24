package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalArea.FunctionalAreaId;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class YamlE2E {
    @JsonProperty(value = "title")
    private final String title;
    @JsonProperty(value = "business-goal")
    private final String businessGoal;
    @JsonProperty(value = "functional-area-id")
    private final FunctionalAreaId functionalAreaId;
    @JsonProperty(value = "jira")
    private final YamlJira jira;

    @JsonProperty(value = "attributes")
    private final List<YamlAttribute> attributes;

    public YamlE2E(@JsonProperty(value = "title") String title,
                   @JsonProperty(value = "business-goal") String businessGoal,
                   @JsonProperty(value = "functional-area-id") FunctionalAreaId functionalAreaId,
                   @JsonProperty(value = "jira") YamlJira jira,
                   @JsonProperty(value = "attributes") List<YamlAttribute> attributes) {
        this.title = title;
        this.businessGoal = businessGoal;
        this.functionalAreaId = functionalAreaId;
        this.jira = jira;
        this.attributes = attributes;
    }

    public boolean hasJiraKeyAndLink() {
        if (null == jira) return false;
        return jira.hasJiraKeyAndLink();
    }

    public static YamlE2E blank() {
        return new YamlE2E("E2E title", "Need to do this", FunctionalAreaId.blank(), YamlJira.blank(), List.of(YamlAttribute.blank()));
    }
}
