package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class YamlId {
    @JsonValue
    private final String id;

    public YamlId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public static YamlId blank() {
        return new YamlId("[SAMPLE-ID]");
    }

}
