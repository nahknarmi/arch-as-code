package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Tdd {
    @JsonProperty(value = "text")
    private final String text;
    @JsonProperty(value = "file")
    private final String file;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Tdd(@JsonProperty("text") String text,
               @JsonProperty("file") String file) {
        this.text = text;
        this.file = file;
    }

    public static Tdd blank() {
        return new Tdd("[SAMPLE TDD TEXT LONG TEXT FORMAT]\nLine 2\nLine 3", null);
    }

    @EqualsAndHashCode
    public static class Id implements EntityReference {
        @JsonValue
        private final String id;

        public Id(String id) {
            this.id = id;
        }

        public static Id blank() {
            return new Id("[SAMPLE-TDD-ID]");
        }
        public static Id noPr() { return new Id("no-PR"); }

        public String toString() {
            return this.id;
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class ComponentReference implements EntityReference {
        @JsonValue
        @Getter
        private final String id;

        public static ComponentReference blank() {
            return new ComponentReference("[SAMPLE-COMPONENT-ID]");
        }

        public String toString() {
            return this.id;
        }
    }
}
