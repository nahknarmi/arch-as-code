package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.nio.file.Files;
import java.util.List;

import static java.nio.file.Files.createTempDirectory;
import static java.util.stream.Collectors.toList;
import static net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateWriter.exportArchitectureUpdate;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.ARCHITECTURE_UPDATE_YML;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateWriterTest {
    private static final FilesFacade files = new FilesFacade();

    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Test
    public void shouldWriteAuWithoutTddContents() throws Exception {
        final var auDir = createTempDirectory("aac");
        final var au = ArchitectureUpdate.blank();

        exportArchitectureUpdate(au, auDir, files);

        final var files = Files.list(auDir)
                .map(path -> path.getFileName().toString())
                .collect(toList());

        collector.checkThat(files, equalTo(List.of(ARCHITECTURE_UPDATE_YML)));
    }
}
