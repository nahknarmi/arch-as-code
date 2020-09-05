package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@ToString
@EqualsAndHashCode
public class Decision {
    @JsonProperty(value = "text")
    private final String text;
    @JsonProperty(value = "tdd-references")
    private final List<Tdd.Id> tddReferences;

    @JsonCreator(mode = PROPERTIES)
    public Decision(
            @JsonProperty("text") String text,
            @JsonProperty("tdd-references") List<Tdd.Id> tddReferences
    ) {
        this.text = text;
        this.tddReferences = tddReferences;
    }

    public static Decision blank() {
        return new Decision("[SAMPLE DECISION TEXT]", List.of(Tdd.Id.blank()));
    }

    @EqualsAndHashCode
    public static class Id implements EntityReference {
        @JsonValue
        @JsonProperty(value = "id")
        private final String id;

        @JsonCreator(mode = PROPERTIES)
        public Id(
                @JsonProperty("id") String id
        ) {
            this.id = id;
        }

        public static Id blank() {
            return new Id("[SAMPLE-DECISION-ID]");
        }

        public String toString() {
            return this.id;
        }
    }
}
