package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class YamlAttribute {
    @JsonProperty(value = "name")
    private final String name;
    @JsonProperty(value = "rationale")
    private final String rationale;
    @JsonProperty(value = "jira")
    private final YamlJira jira;

    public YamlAttribute(@JsonProperty("name") String name, @JsonProperty("rationale") String rationale, @JsonProperty("jira") YamlJira jira) {
        this.name = name;
        this.rationale = rationale;
        this.jira = jira;
    }

    public static YamlAttribute blank() {
        return new YamlAttribute("Accessible", "UI need to be Accessible", YamlJira.blank());
    }
}
