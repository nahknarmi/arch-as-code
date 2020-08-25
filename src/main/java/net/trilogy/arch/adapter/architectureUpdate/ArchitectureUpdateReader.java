package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArchitectureUpdateReader {
    public static final String ARCHITECTURE_UPDATE_YML = "architecture-update.yml";
    private final FilesFacade filesFacade;

    public ArchitectureUpdateReader(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    public ArchitectureUpdate load(Path path) throws IOException {
        String auAsString = filesFacade.readString(path.resolve(ARCHITECTURE_UPDATE_YML));
        ArchitectureUpdate au = new ArchitectureUpdateObjectMapper().readValue(auAsString);

        List<TddContent> tddContents = getTddContent(path);
        au = au.toBuilder().tddContents(tddContents).build();

        au = assignTddContents(au, tddContents);

        return au;
    }

    private ArchitectureUpdate assignTddContents(ArchitectureUpdate au, List<TddContent> tddContents) {
        au.getTddContainersByComponent().stream().forEach(componentTdds -> {
            componentTdds.getTdds().entrySet().stream().forEach(tdd -> {
                Optional<TddContent> tddContent = contentByMatchingIds(tddContents, componentTdds, tdd.getKey());
                tdd.getValue().setContent(tddContent);
            });
        });
        return au;
    }

    private Optional<TddContent> contentByMatchingIds(List<TddContent> tddContents, TddContainerByComponent componentTdds, Tdd.Id tddId) {
        return tddContents.stream()
                .filter(content -> content.getTdd().equals(tddId.toString()))
                .filter(content -> content.getComponentId().equals(componentTdds.getComponentId().getId()))
                .findFirst();
    }

    private Optional<TddContent> contentByMatchingFileName(List<TddContent> tddContents, Tdd tdd) {
        if(tdd.getFile() == null){
            return Optional.empty();
        }
        return tddContents.stream().filter(content -> content.getFilename().equals(tdd.getFile())).findFirst();
    }

    private List<TddContent> getTddContent(Path path) {
        final File[] files = path.toFile().listFiles();

        return Arrays.stream(files)
                .filter(TddContent::isContentType)
                .filter(TddContent::isTddContentName)
                .map((File file) -> TddContent.createCreateFromFile(file, filesFacade))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
