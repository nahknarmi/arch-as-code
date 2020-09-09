package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.nio.file.Files;
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
        final var auDir = Files.createTempDirectory("aac");
        final var tddContentFilename = "TDD 1.0 : Component-100.md";
        final var tddContentFilename2 = "TDD 2.0 : Component-200.md";
        final var content = "## Title\n### Content\nLorem ipsum";
        final var au = ArchitectureUpdate.blank().toBuilder()
                .tddContents(List.of(
                        new TddContent(content, tddContentFilename),
                        new TddContent("content", tddContentFilename2)))
                .build();

        // When
        new ArchitectureUpdateWriter(new FilesFacade()).exportArchitectureUpdate(au, auDir);

        // Then
        final var tddContent = Files.readString(auDir.resolve(tddContentFilename));
        final var tddContents = Files.readString(auDir.resolve(tddContentFilename2));
        final var auAsString = Files.readString(auDir.resolve("architecture-update.yml"));

        collector.checkThat(tddContent, equalTo(content));
        collector.checkThat(tddContents, equalTo("content"));
        collector.checkThat(auAsString, containsString("name: '[SAMPLE NAME]'\n"));
    }

    @Test
    public void shouldWriteAuWithoutTddContents() throws Exception {
        final var auDir = Files.createTempDirectory("aac");
        final var au = ArchitectureUpdate.blank();

        new ArchitectureUpdateWriter(new FilesFacade()).exportArchitectureUpdate(au, auDir);

        final var files = Files.list(auDir)
                .map(path -> path.getFileName().toString())
                .collect(toList());

        collector.checkThat(files, equalTo(List.of("architecture-update.yml")));
    }
}
