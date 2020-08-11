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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateWriterTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();


    @Test
    public void shouldWriteAuAndTddContentToAuDirectory() throws Exception {
        // Given
        Path auDir = Files.createTempDirectory("aac");
        String tddContentFilename = "TDD 1.0 : Component-100.md";
        String tddContentFilename2 = "TDD 2.0 : Component-200.md";
        String content = "## Title\n### Content\nLorem ipsum";
        ArchitectureUpdate au = ArchitectureUpdate.blank().toBuilder()
                .tddContents(List.of(
                        new TddContent(content, tddContentFilename),
                        new TddContent("content", tddContentFilename2)
                )).build();

        // When
        new ArchitectureUpdateWriter(new FilesFacade()).export(au, auDir);

        // Then
        String tddContent = Files.readString(auDir.resolve(tddContentFilename));
        String tddContents = Files.readString(auDir.resolve(tddContentFilename2));
        String auAsString = Files.readString(auDir.resolve("architecture-update.yml"));
        collector.checkThat(tddContent, equalTo(content));
        collector.checkThat(tddContents, equalTo("content"));
        collector.checkThat(auAsString, containsString("name: '[SAMPLE NAME]'\n"));
    }

    @Test
    public void shouldWriteAuWithoutTddContents() throws Exception {
        Path auDir = Files.createTempDirectory("aac");
        ArchitectureUpdate au = ArchitectureUpdate.blank();

        new ArchitectureUpdateWriter(new FilesFacade()).export(au, auDir);

        List<String> files = Files.list(auDir)
                .map(path -> path.getFileName().toString())
                .collect(toList());

        collector.checkThat(files, equalTo(List.of("architecture-update.yml")));
    }
}
