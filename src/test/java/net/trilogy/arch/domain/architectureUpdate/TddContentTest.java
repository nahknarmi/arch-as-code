package net.trilogy.arch.domain.architectureUpdate;

import net.trilogy.arch.facade.FilesFacade;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TddContentTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();


    @Test
    public void shouldIdentifyAllowedFileTypes() throws IOException {
        File tempMarkdown = Files.createTempFile("markdown", ".md").toFile();
        File tempText = Files.createTempFile("text", ".txt").toFile();
        File tempPdf = Files.createTempFile("acrobat", ".pdf").toFile();
        File dir = Files.createTempDirectory("temp").toFile();
        File nullFile = null;

        collector.checkThat(TddContent.isContentType(tempMarkdown), equalTo(true));
        collector.checkThat(TddContent.isContentType(tempText), equalTo(true));
        collector.checkThat(TddContent.isContentType(tempPdf), equalTo(false));
        collector.checkThat(TddContent.isContentType(dir), equalTo(false));
        collector.checkThat(TddContent.isContentType(nullFile), equalTo(false));
    }

    @Test
    public void shouldIdentifyFilesThatMatchTddContentNamingConvention() throws IOException {
        File tempMarkdown = Files.createTempFile("markdown", ".md").toFile();
        File tddMarkdown = Files.createTempFile("TDD 1.1 : Component-10", ".md").toFile();
        File tddText = Files.createTempFile("IFD 2.3.4 : Component-101", ".txt").toFile();
        File arbitraryIdText = Files.createTempFile("TDD 1.0 : Component-ArbitraryId42", ".txt").toFile();

        collector.checkThat(TddContent.isTddContentName(tempMarkdown), equalTo(false));
        collector.checkThat(TddContent.isTddContentName(tddMarkdown), equalTo(true));
        collector.checkThat(TddContent.isTddContentName(tddText), equalTo(true));
        collector.checkThat(TddContent.isTddContentName(arbitraryIdText), equalTo(true));

    }

    @Test
    public void shouldCreateContentFromFiles() throws IOException {
        Path rootDir = Files.createTempDirectory("temp");
        Path file = new FilesFacade().writeString(rootDir.resolve("markdown.md"), "contents");

        TddContent tddContent = TddContent.createCreateFromFile(file.toFile(), new FilesFacade());

        collector.checkThat(tddContent.getContent(), equalTo("contents"));
        collector.checkThat(tddContent.getFilename(), equalTo("markdown.md"));
    }

    @Test
    public void shouldReturnNullIfFileNull() {
        TddContent nullContent = TddContent.createCreateFromFile(null, new FilesFacade());

        collector.checkThat(nullContent, equalTo(null));
    }

    @Test
    public void shouldReturnNullIfError() throws Exception {
        // Given
        Path rootDir = Files.createTempDirectory("temp");
        Path file = new FilesFacade().writeString(rootDir.resolve("markdown.md"), "contents");

        FilesFacade filesFacade = mock(FilesFacade.class);
        when(filesFacade.readString(any())).thenThrow(new IOException("boom"));

        // When
        TddContent content = TddContent.createCreateFromFile(file.toFile(), filesFacade);

        // Then
        collector.checkThat(content, equalTo(null));
    }

    @Test
    public void shouldParseTddComponentFromFilename() {
        TddContent tddContent = new TddContent("content", "TDD 1.1 : Component-10.md");

        collector.checkThat(tddContent.getTdd(), equalTo("TDD 1.1"));
        collector.checkThat(tddContent.getComponentId(), equalTo("10"));
    }
}
