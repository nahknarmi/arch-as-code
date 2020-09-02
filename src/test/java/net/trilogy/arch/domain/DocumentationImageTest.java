package net.trilogy.arch.domain;

import net.trilogy.arch.TestHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.Matchers.equalTo;

public class DocumentationImageTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Test
    public void shouldIdentifyImages() throws IOException {
        File image = new File(getClass().getResource(TestHelper.IMAGE_THOUGHTWORKS_FILE).getFile());
        File notImage = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_EMPTY).getFile());
        File dir = Files.createTempDirectory("aac").toFile();

        collector.checkThat(DocumentationImage.isImage(image), equalTo(true));
        collector.checkThat(DocumentationImage.isImage(notImage), equalTo(false));
        collector.checkThat(DocumentationImage.isImage(dir), equalTo(false));
        collector.checkThat(DocumentationImage.isImage(null), equalTo(false));
    }
}
