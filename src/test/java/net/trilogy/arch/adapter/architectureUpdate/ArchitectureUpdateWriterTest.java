package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateWriterTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();


    @Test
    public void shouldWriteAuAndTddContentToAuDirectory() throws Exception {
        // Given
        Path auDir = Files.createTempDirectory("aac");
        String tddContentFilename = "TDD 1.1 : Component-123.md";
        String content = "## Title\n### Content\nLorem ipsum";
        var au = ArchitectureUpdate.blank().toBuilder()
                .tddContents(List.of(
                        new TddContent(content, tddContentFilename)
                )).build();

        // When
        new ArchitectureUpdateWriter(new FilesFacade()).export(au, auDir);

        // Then
        String tddContent = Files.readString(auDir.resolve(tddContentFilename));
        String auAsString = Files.readString(auDir.resolve("architecture-update.yml"));
        collector.checkThat(tddContent, equalTo(content));
        collector.checkThat(auAsString, containsString("name: '[SAMPLE NAME]'\n"));
    }
}
