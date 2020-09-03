package net.trilogy.arch.adapter.architectureDataStructure;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;

/**
 * This class is being strangle-patterned away. Use {@link
 * net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper
 * }
 */
@Deprecated
public class ArchitectureDataStructureReader {

    final private FilesFacade filesFacade;

    public ArchitectureDataStructureReader(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    public ArchitectureDataStructure load(File manifest) throws IOException {
        final String archAsString = filesFacade.readString(manifest.toPath());
        return new ArchitectureDataStructureObjectMapper().readValue(archAsString);
    }
}
