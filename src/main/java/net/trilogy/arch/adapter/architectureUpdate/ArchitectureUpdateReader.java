package net.trilogy.arch.adapter.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.ARCHITECTURE_UPDATE_YML;

@RequiredArgsConstructor
public class ArchitectureUpdateReader {
    private final FilesFacade filesFacade;

    private static ArchitectureUpdate assignTddContents(ArchitectureUpdate au, List<TddContent> tddContents) {
        final var componentsWithTddsContents = au.getTddContainersByComponent().stream()
                .map(it -> it.updateTddContents(tddContents))
                .collect(toList());

        return au.toBuilder()
                .tddContainersByComponent(componentsWithTddsContents)
                .build();
    }

    public ArchitectureUpdate loadArchitectureUpdate(Path path) throws IOException {
        final var auAsString = filesFacade.readString(path.resolve(ARCHITECTURE_UPDATE_YML));
        final var au = YAML_OBJECT_MAPPER.readValue(auAsString, ArchitectureUpdate.class);

        return assignTddContents(au, getTddContent(path));
    }

    private List<TddContent> getTddContent(Path path) {
        return stream(requireNonNull(path.toFile().listFiles()))
                .filter(TddContent::isContentType)
                .filter(TddContent::isTddContentName)
                .map((File file) -> TddContent.createCreateFromFile(file, filesFacade))
                .filter(Objects::nonNull)
                .collect(toList());
    }
}
