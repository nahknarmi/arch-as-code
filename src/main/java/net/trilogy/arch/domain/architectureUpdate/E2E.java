package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.FunctionalArea.FunctionalAreaId;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class E2E {

    @JsonProperty(value = "title")
    private final String title;
    @JsonProperty(value = "business-goal")
    private final String businessGoal;
    @JsonProperty(value = "functional-area-id")
    private final FunctionalAreaId functionalAreaId;
    @JsonProperty(value = "jira")
    private final Jira jira;

    @JsonProperty(value = "attributes")
    private final List<Attribute> attributes;

    public E2E(@JsonProperty(value = "title") String title,
               @JsonProperty(value = "business-goal") String businessGoal,
               @JsonProperty(value = "functional-area-id") FunctionalAreaId functionalAreaId,
               @JsonProperty(value = "jira") Jira jira,
               @JsonProperty(value = "attributes") List<Attribute> attributes) {
        this.title = title;
        this.businessGoal = businessGoal;
        this.functionalAreaId = functionalAreaId;
        this.jira = jira;
        this.attributes = attributes;
    }

    public static E2E blank() {
        return new E2E("E2E title", "Need to do this", FunctionalAreaId.blank(), Jira.blank(), List.of(Attribute.blank()));
    }
}
