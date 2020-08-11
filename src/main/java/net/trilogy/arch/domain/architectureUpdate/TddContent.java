package net.trilogy.arch.domain.architectureUpdate;

import com.google.common.io.Files;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

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

    public static boolean isContent(File file) {
        if (file == null) return false;
        if (file.isDirectory()) return false;

        String fileExtension = Files.getFileExtension(file.getName());

        // Supported content types
        return fileExtension.equals("md") || fileExtension.equals("txt");
    }
}
