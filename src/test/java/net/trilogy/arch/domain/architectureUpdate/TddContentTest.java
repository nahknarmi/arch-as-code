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

        collector.checkThat(TddContent.isContent(tempMarkdown), equalTo(true));
        collector.checkThat(TddContent.isContent(tempText), equalTo(true));
        collector.checkThat(TddContent.isContent(tempPdf), equalTo(false));
        collector.checkThat(TddContent.isContent(dir), equalTo(false));
        collector.checkThat(TddContent.isContent(nullFile), equalTo(false));
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
        FilesFacade filesFacade = mock(FilesFacade.class);
        when(filesFacade.readString(any())).thenThrow(new RuntimeException("boom"));

        TddContent content = TddContent.createCreateFromFile(null, filesFacade);

        collector.checkThat(content, equalTo(null));
    }
}
