package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;

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
    private final List<TddId> tddReferences;

    @JsonCreator(mode = PROPERTIES)
    public FunctionalRequirement(
            @JsonProperty("text") String text,
            @JsonProperty("source") String source,
            @JsonProperty("tdd-references") List<TddId> tddReferences) {
        this.text = text;
        this.source = source;
        this.tddReferences = tddReferences;
    }

    public static FunctionalRequirement blank() {
        return new FunctionalRequirement("[SAMPLE REQUIREMENT TEXT]", "[SAMPLE REQUIREMENT SOURCE TEXT]", List.of(TddId.blank()));
    }

    @EqualsAndHashCode
    public static class FunctionalRequirementId implements EntityReference {
        @JsonValue
        private final String id;

        public FunctionalRequirementId(String id) {
            this.id = id;
        }

        public static FunctionalRequirementId blank() {
            return new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]");
        }

        public String toString() {
            return this.id;
        }
    }
}
