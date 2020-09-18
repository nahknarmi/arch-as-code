package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Attribute {
    @JsonProperty(value = "name")
    private final String name;
    @JsonProperty(value = "rationale")
    private final String rationale;
    @JsonProperty(value = "jira")
    private final Jira jira;

    public Attribute(@JsonProperty("name") String name,@JsonProperty("rationale") String rationale,@JsonProperty("jira") Jira jira) {
        this.name = name;
        this.rationale = rationale;
        this.jira = jira;
    }

    public static Attribute blank() {
        return new Attribute("Accessible", "UI need to be Accessible", Jira.blank());
    }
}
