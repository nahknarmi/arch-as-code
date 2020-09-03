package net.trilogy.arch.domain;

import com.structurizr.documentation.Format;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.domain.DocumentationSection.Format.ASCIIDOC;
import static net.trilogy.arch.domain.DocumentationSection.Format.MARKDOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DocumentationSectionTest {
    @Test
    public void shouldReturnStructurizrFormat() {
        final DocumentationSection markdownDoc = new DocumentationSection("0", "title", 1, MARKDOWN, "markdown content");
        final DocumentationSection asciiDoc = new DocumentationSection("0", "title", 1, ASCIIDOC, "markdown content");

        assertThat(markdownDoc.getStructurizrFormat(), equalTo(Format.Markdown));
        assertThat(asciiDoc.getStructurizrFormat(), equalTo(Format.AsciiDoc));
    }

    @Test
    public void shouldCreateMarkdownDocumentationFromFile() throws Exception {
        final File markdown = new File(getClass().getResource(TestHelper.MARKDOWN_DOCUMENTATION_FILE).getPath());
        final DocumentationSection doc = DocumentationSection.createFromFile(markdown, new FilesFacade());

        assertThat(doc.getTitle(), equalTo("context-diagram"));
        assertThat(doc.getOrder(), equalTo(1));
        assertThat(doc.getFormat(), equalTo(MARKDOWN));
        assertThat(doc.getContent(), equalTo("DevSpaces operates with the following elements in mind:\n\nA **client running on the developer’s local workstation** and a server running in the cloud that works together to seamlessly run workloads in the cloud while keeping the developer experience local. \n\nThe client initiates execution requests, synchronizes filesystem changes, and gives developers a view of the execution results.\n\nThe software developer is able to deploy one or more pods/containers in a DevSpace using a kubernetes configuration file... \n\nThe **DevSpaces server** stores the configuration of the DevSpace definitions and synchronizes changes back to the client.\n\nThe **container management platform** composed of a highly scalable, high-performance Kubernetes container management service that supports Docker containers and allows you to easily run applications on a managed cluster.\n"));
    }

    @Test
    public void shouldCreateAsciiDocumentationFromFile() throws Exception {
        final File markdown = new File(getClass().getResource(TestHelper.ASCII_DOCUMENTATION_FILE).getPath());
        final DocumentationSection doc = DocumentationSection.createFromFile(markdown, new FilesFacade());

        assertThat(doc.getTitle(), equalTo("Ascii-docs"));
        assertThat(doc.getOrder(), equalTo(3));
        assertThat(doc.getFormat(), equalTo(ASCIIDOC));
        assertThat(doc.getContent(), equalTo("                                                                                                            \n                                                                                                            \n   ,---,                                                          ,---,                                     \n  '  .' \\                             ,--,    ,--,              .'  .' `\\                                   \n /  ;    '.                         ,--.'|  ,--.'|      ,---,.,---.'     \\    ,---.                         \n:  :       \\    .--.--.             |  |,   |  |,     ,'  .' ||   |  .`\\  |  '   ,'\\             .--.--.    \n:  |   /\\   \\  /  /    '     ,---.  `--'_   `--'_   ,---.'   ,:   : |  '  | /   /   |   ,---.   /  /    '   \n|  :  ' ;.   :|  :  /`./    /     \\ ,' ,'|  ,' ,'|  |   |    ||   ' '  ;  :.   ; ,. :  /     \\ |  :  /`./   \n|  |  ;/  \\   \\  :  ;_     /    / ' '  | |  '  | |  :   :  .' '   | ;  .  |'   | |: : /    / ' |  :  ;_     \n'  :  | \\  \\ ,'\\  \\    `. .    ' /  |  | :  |  | :  :   |.'   |   | :  |  ''   | .; :.    ' /   \\  \\    `.  \n|  |  '  '--'   `----.   \\'   ; :__ '  : |__'  : |__`---'     '   : | /  ; |   :    |'   ; :__   `----.   \\ \n|  :  :        /  /`--'  /'   | '.'||  | '.'|  | '.'|         |   | '` ,/   \\   \\  / '   | '.'| /  /`--'  / \n|  | ,'       '--'.     / |   :    :;  :    ;  :    ;         ;   :  .'      `----'  |   :    :'--'.     /  \n`--''           `--'---'   \\   \\  / |  ,   /|  ,   /          |   ,.'                 \\   \\  /   `--'---'   \n                            `----'   ---`-'  ---`-'           '---'                    `----'               \n"));
    }

    @Test
    public void shouldAcceptFilenameWithMissingOrder() throws Exception {
        final File markdown = new File(getClass().getResource(TestHelper.NO_ORDER_DOCUMENTATION_FILE).getPath());
        final Path content = Files.writeString(markdown.toPath(), "content");

        final DocumentationSection doc = DocumentationSection.createFromFile(markdown, new FilesFacade());

        assertThat(doc.getTitle(), equalTo("no_order"));
        assertThat(doc.getFormat(), equalTo(ASCIIDOC));
        assertThat(doc.getContent(), equalTo("content"));
        assertThat(doc.getOrder(), equalTo(null));
    }

    @Test
    public void shouldCalculateDocumentationFilename() {
        final DocumentationSection orderedDoc = new DocumentationSection(null, "OrderedTitle", 1, MARKDOWN, "");
        final DocumentationSection unorderedDoc = new DocumentationSection(null, "UnorderedTitle", null, MARKDOWN, "");
        final DocumentationSection orderedNoExtension = new DocumentationSection(null, "NoExtension", 2, null, "");
        final DocumentationSection unorderedNoExtension = new DocumentationSection(null, "NoExtension", null, null, "");

        assertThat(orderedDoc.getFileName(), equalTo("1_OrderedTitle.md"));
        assertThat(unorderedDoc.getFileName(), equalTo("UnorderedTitle.md"));
        assertThat(orderedNoExtension.getFileName(), equalTo("2_NoExtension"));
        assertThat(unorderedNoExtension.getFileName(), equalTo("NoExtension"));
    }
}
