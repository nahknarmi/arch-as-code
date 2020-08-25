package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class ArchitectureUpdateReader {
    public static final String ARCHITECTURE_UPDATE_YML = "architecture-update.yml";
    private final FilesFacade filesFacade;

    public ArchitectureUpdateReader(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    public ArchitectureUpdate load(Path path) throws IOException {
        final var auAsString = filesFacade.readString(path.resolve(ARCHITECTURE_UPDATE_YML));
        var au = new ArchitectureUpdateObjectMapper().readValue(auAsString);

        // TODO: Mutable object are bug-prone and dangerous and it confuses
        //       that the content path is added twice
        au = au.toBuilder().tddContents(getTddContent(path)).build();

        return assignTddContents(au, getTddContent(path));
    }

    private ArchitectureUpdate assignTddContents(ArchitectureUpdate au, List<TddContent> tddContents) {
        au.getTddContainersByComponent().forEach(componentTdds -> componentTdds.getTdds().forEach((key, value) -> {
            Optional<TddContent> tddContent = contentByMatchingIds(tddContents, componentTdds, key);
            value.setContent(tddContent);
        }));
        return au;
    }

    private Optional<TddContent> contentByMatchingIds(List<TddContent> tddContents, TddContainerByComponent componentTdds, Tdd.Id tddId) {
        return tddContents.stream()
                .filter(content -> content.getTdd().equals(tddId.toString()))
                .filter(content -> content.getComponentId().equals(componentTdds.getComponentId().getId()))
                .findFirst();
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
