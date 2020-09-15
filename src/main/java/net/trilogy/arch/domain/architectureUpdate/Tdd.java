package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@ToString
@EqualsAndHashCode
public class Tdd {
    @JsonProperty(value = "text")
    private final String text;
    // TODO: Why isn't this a JDK Path object?
    @JsonProperty(value = "file")
    private final String file;

    @Getter
    @Setter
    @JsonIgnore
    private TddContent content = null;

    @JsonCreator(mode = PROPERTIES)
    public Tdd(@JsonProperty("text") String text,
               @JsonProperty("file") String file) {
        this.text = text;
        this.file = file;
    }

    public Tdd(String text,
               String file,
               TddContent tddContent) {
        this.text = text;
        this.file = file;
        this.content = tddContent;
    }

    public static Tdd blank() {
        return new Tdd("[SAMPLE TDD TEXT LONG TEXT FORMAT]\nLine 2\nLine 3", null);
    }

    public String getDetails() {
        return null == content ? text : content.getContent();
    }

    @EqualsAndHashCode
    public static class TddId implements EntityReference {
        @JsonValue
        private final String id;

        public TddId(String id) {
            this.id = id;
        }

        public static TddId blank() {
            return new TddId("[SAMPLE-TDD-ID]");
        }

        public static TddId noPr() {
            return new TddId("no-PR");
        }

        public String toString() {
            return id;
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class TddComponentReference implements EntityReference {
        @JsonValue
        @Getter
        private final String id;

        public static TddComponentReference blank() {
            return new TddComponentReference("[SAMPLE-COMPONENT-ID]");
        }

        public String toString() {
            return id;
        }
    }
}

