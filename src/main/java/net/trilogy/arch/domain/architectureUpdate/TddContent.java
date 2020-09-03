package net.trilogy.arch.domain.architectureUpdate;

import com.google.common.io.Files;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@ToString
@EqualsAndHashCode
public class TddContent {
    public static final int TDD_MATCHER_GROUP = 1;
    public static final int COMPONENT_ID_MATCHER_GROUP = 2;
    private static final String REGEX = "(.*) : Component-([a-zA-Z\\d]+)";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private final String content;
    private final String filename;
    @EqualsAndHashCode.Exclude
    private Matcher matcher;

    public TddContent(String content, String filename) {
        this.content = content;
        this.filename = filename;
    }

    public static boolean isContentType(File file) {
        if (file == null) return false;
        if (file.isDirectory()) return false;

        String fileExtension = Files.getFileExtension(file.getName());

        // Supported content types
        return fileExtension.equals("md") || fileExtension.equals("txt");
    }

    public static boolean isTddContentName(File file) {
        if (file == null) return false;
        if (file.isDirectory()) return false;

        return pattern.matcher(file.getName()).find();
    }

    public static TddContent createCreateFromFile(File file, FilesFacade filesFacade) {
        if (file == null) return null;

        String content;
        String filename;

        try {
            content = filesFacade.readString(file.toPath());
            filename = file.getName();
        } catch (IOException e) {
            return null;
        }

        return new TddContent(content, filename);
    }

    public String getTdd() {
        return matcher().group(TDD_MATCHER_GROUP);
    }

    public String getComponentId() {
        return matcher().group(COMPONENT_ID_MATCHER_GROUP);
    }

    private Matcher matcher() {
        if (matcher == null) {
            matcher = pattern.matcher(filename);
            matcher.find();
        }

        return this.matcher;
    }
}
