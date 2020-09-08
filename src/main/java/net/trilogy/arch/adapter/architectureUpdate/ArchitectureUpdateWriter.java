package net.trilogy.arch.adapter.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.nio.file.Path;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;

@RequiredArgsConstructor
public class ArchitectureUpdateWriter {
    private final FilesFacade filesFacade;

    public void export(ArchitectureUpdate au, Path path) throws IOException {
        final var auPath = path.resolve("architecture-update.yml");

        filesFacade.writeString(auPath, YAML_OBJECT_MAPPER.writeValueAsString(au));

        writeTddContents(au, path);
    }

    private void writeTddContents(ArchitectureUpdate au, Path path) throws IOException {
        if (au.getTddContents() == null || au.getTddContents().isEmpty())
            return;

        for (final var tdd : au.getTddContents()) {
            filesFacade.writeString(path.resolve(tdd.getFilename()), tdd.getContent());
        }
    }
}
