package net.trilogy.arch.adapter.architectureUpdate;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.nio.file.Path;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;

@UtilityClass
public class ArchitectureUpdateWriter {
    public static void exportArchitectureUpdate(ArchitectureUpdate au, Path path, FilesFacade files) throws IOException {
        final var auPath = path.resolve("architecture-update.yml");

        files.writeString(auPath, YAML_OBJECT_MAPPER.writeValueAsString(au));

        writeTddContents(au, path, files);
    }

    private static void writeTddContents(ArchitectureUpdate au, Path path, FilesFacade files) throws IOException {
        if (au.getTddContents() == null || au.getTddContents().isEmpty())
            return;

        for (final var tdd : au.getTddContents()) {
            files.writeString(path.resolve(tdd.getFilename()), tdd.getContent());
        }
    }
}
