package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@EqualsAndHashCode
public class Person {
    @JsonProperty(value = "name")
    private final String name;
    @JsonProperty(value = "email")
    private final String email;

    @JsonCreator(mode = PROPERTIES)
    public Person(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email
    ) {
        this.name = name;
        this.email = email;
    }

    public static Person blank() {
        return new Person("[SAMPLE PERSON NAME]", "[SAMPLE PERSON EMAIL]");
    }
}
