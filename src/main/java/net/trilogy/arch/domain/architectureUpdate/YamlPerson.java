package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@EqualsAndHashCode
@ToString
public class YamlPerson {
    @JsonProperty(value = "name")
    private final String name;
    @JsonProperty(value = "email")
    private final String email;

    @JsonCreator(mode = PROPERTIES)
    public YamlPerson(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email) {
        this.name = name;
        this.email = email;
    }

    public static YamlPerson blank() {
        return new YamlPerson("[SAMPLE PERSON NAME]", "[SAMPLE PERSON EMAIL]");
    }
}
