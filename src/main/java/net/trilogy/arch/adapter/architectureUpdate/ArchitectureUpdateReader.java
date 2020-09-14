package net.trilogy.arch.adapter.architectureUpdate;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
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

    public ArchitectureUpdate loadArchitectureUpdate(Path path) throws IOException {
        final var auAsString = filesFacade.readString(path.resolve(ARCHITECTURE_UPDATE_YML));
        var au = YAML_OBJECT_MAPPER.readValue(auAsString, ArchitectureUpdate.class);

        return assignTddContents(au, getTddContent(path));
    }

    private static ArchitectureUpdate assignTddContents(ArchitectureUpdate au, List<TddContent> tddContents) {
        au.getTddContainersByComponent().forEach(componentTdds -> componentTdds.getTdds().forEach((tddId, tdd) -> {
            final var tddContent = contentByMatchingIds(tddContents, componentTdds, tddId);
            tdd.setContent(tddContent);
        }));
        return au;
    }

    static TddContent contentByMatchingIds(List<TddContent> tddContents, TddContainerByComponent componentTdds, TddId tddId) {
        return tddContents.stream()
                .filter(content -> content.getTdd().equals(tddId.toString()))
                .filter(content -> componentTdds.getComponentId() != null && content.getComponentId().equals(componentTdds.getComponentId().getId()))
                .findFirst()
                .orElse(null);
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
