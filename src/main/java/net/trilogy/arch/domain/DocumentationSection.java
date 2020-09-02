package net.trilogy.arch.domain;

import com.google.common.io.Files;
import lombok.Data;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;

import static com.structurizr.documentation.Format.AsciiDoc;
import static com.structurizr.documentation.Format.Markdown;

@Data
public class DocumentationSection {
    private final String elementId;
    private final String title;
    private final Integer order;
    private final Format format;
    private final String content;

    public static DocumentationSection createFromFile(File file, FilesFacade filesFacade) throws IOException {
        if (file == null || file.isDirectory()) return null;

        final String fullName = file.getName();

        final Integer order = getOrderFromFullname(fullName);
        final String title = getTitleFromFullname(fullName, order);
        final Format format = formatFromExtension(Files.getFileExtension(fullName));
        final String content = filesFacade.readString(file.toPath());

        return new DocumentationSection(null, title, order, format, content);
    }

    private static String getTitleFromFullname(String fullName, Integer order) {
        final String nameWithoutExtension = Files.getNameWithoutExtension(fullName);

        if (order == null) return nameWithoutExtension;

        final String[] s = nameWithoutExtension.split("_", 2);

        if (s.length == 1) return s[0];

        return s[1];
    }

    private static Format formatFromExtension(String extension) {
        if (extension.equals("md")) return Format.MARKDOWN;

        return Format.ASCIIDOC;
    }

    private static Integer getOrderFromFullname(String name) {
        final String[] s = name.split("_", 2);

        if (s.length == 1) return null;

        Integer order = null;
        try {
            order = Integer.parseInt(s[0]);
        } catch (NumberFormatException ignore) {
        }

        return order;
    }

    public String getFileName() {
        if (getOrder() == null)
            return title + extensionFromFormat(getFormat());

        return getOrder().toString() + "_" + title + extensionFromFormat(getFormat());
    }

    public com.structurizr.documentation.Format getStructurizrFormat() {
        if (getFormat().equals(Format.MARKDOWN)) return Markdown;

        return AsciiDoc;
    }

    private String extensionFromFormat(Format format) {
        if (format == null) return "";
        if (format.equals(Format.MARKDOWN)) return ".md";

        return ".txt";
    }

    public enum Format {
        MARKDOWN,
        ASCIIDOC
    }
}
