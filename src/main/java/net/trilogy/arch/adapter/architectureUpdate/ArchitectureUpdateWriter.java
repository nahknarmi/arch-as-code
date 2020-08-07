package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.nio.file.Path;

public class ArchitectureUpdateWriter {
    private final FilesFacade filesFacade;

    public ArchitectureUpdateWriter(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    public void export(ArchitectureUpdate au, Path path) throws IOException {
        ArchitectureUpdateObjectMapper mapper = new ArchitectureUpdateObjectMapper();
        Path auPath = path.resolve("architecture-update.yml");
        filesFacade.writeString(auPath, mapper.writeValueAsString(au));

        if (au.getTddContents() != null && !au.getTddContents().isEmpty()) {
            au.getTddContents().forEach(tdd -> {
                try {
                    filesFacade.writeString(path.resolve(tdd.getFilename()), tdd.getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
