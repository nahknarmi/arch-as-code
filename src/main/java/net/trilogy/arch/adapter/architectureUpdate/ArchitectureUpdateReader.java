package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

        return au;
    }

    private List<TddContent> getTddContent(Path path) {
        final File[] files = path.toFile().listFiles();

        return Arrays.stream(files)
                .filter(TddContent::isContentType)
                .map((File file) -> TddContent.createCreateFromFile(file, filesFacade))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
