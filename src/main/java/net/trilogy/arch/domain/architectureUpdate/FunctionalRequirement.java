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
public class FunctionalRequirement {
    @JsonProperty(value = "text")
    private final String text;
    @JsonProperty(value = "source")
    private final String source;
    @JsonProperty(value = "tdd-references")
    private final List<Tdd.Id> tddReferences;

    @JsonCreator(mode = PROPERTIES)
    public FunctionalRequirement(
            @JsonProperty("text") String text,
            @JsonProperty("source") String source,
            @JsonProperty("tdd-references") List<Tdd.Id> tddReferences) {
        this.text = text;
        this.source = source;
        this.tddReferences = tddReferences;
    }

    public static FunctionalRequirement blank() {
        return new FunctionalRequirement("[SAMPLE REQUIREMENT TEXT]", "[SAMPLE REQUIREMENT SOURCE TEXT]", List.of(Tdd.Id.blank()));
    }

    @EqualsAndHashCode
    public static class Id implements EntityReference {
        @JsonValue
        private final String id;

        public Id(String id) {
            this.id = id;
        }

        public static Id blank() {
            return new Id("[SAMPLE-REQUIREMENT-ID]");
        }

        public String toString() {
            return this.id;
        }
    }
}
