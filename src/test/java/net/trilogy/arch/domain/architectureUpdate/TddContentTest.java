package net.trilogy.arch.domain.architectureUpdate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.Matchers.equalTo;

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
}
