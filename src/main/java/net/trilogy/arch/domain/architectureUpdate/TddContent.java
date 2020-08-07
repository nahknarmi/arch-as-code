package net.trilogy.arch.domain.architectureUpdate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class TddContent {
    private final String content;
    private final String filename;

    public TddContent(String content, String filename) {
        this.content = content;
        this.filename = filename;
    }
}
