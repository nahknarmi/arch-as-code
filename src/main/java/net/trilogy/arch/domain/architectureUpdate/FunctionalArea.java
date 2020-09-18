package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@ToString
@EqualsAndHashCode
public class FunctionalArea {
    @JsonProperty(value = "title")
    private final String title;
    @JsonProperty(value = "jira")
    private final Jira jira;

    @JsonCreator(mode = PROPERTIES)
    public FunctionalArea(@JsonProperty("title") String title, @JsonProperty("jira") Jira jira) {
        this.title = title;
        this.jira = jira;
    }

    public static FunctionalArea blank() {
        return new FunctionalArea("Sample title", Jira.blank());
    }

    public static class FunctionalAreaId extends YamlId implements EntityReference {

        public FunctionalAreaId(String id) {
            super(id);
        }

        public static FunctionalAreaId blank() {
            return new FunctionalAreaId("[Sample Functional Area Id]");
        }
    }
}
