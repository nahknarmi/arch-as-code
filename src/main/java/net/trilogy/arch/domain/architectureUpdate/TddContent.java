package net.trilogy.arch.domain.architectureUpdate;

import com.google.common.io.Files;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;

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

    public static TddContent createCreateFromFile(File file, FilesFacade filesFacade) {
        if (file == null) return null;

        String content = null;
        String filename = null;

        try {
            content = filesFacade.readString(file.toPath());
            filename = file.getName();
        } catch (IOException e) {
            return null;
        }

        return new TddContent(content, filename);
    }
}
