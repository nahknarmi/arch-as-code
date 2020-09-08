package net.trilogy.arch.adapter.architectureDataStructure;

import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;

import java.io.File;
import java.io.IOException;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.ADS_OBJECT_MAPPER;

/**
 * This class is being strangle-patterned away. Use {@link
 * net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper
 * }
 */
@Deprecated
@RequiredArgsConstructor
public class ArchitectureDataStructureReader {
    private final FilesFacade filesFacade;

    public ArchitectureDataStructure load(File manifest) throws IOException {
        final String architectureAsString = filesFacade.readString(manifest.toPath());
        return ADS_OBJECT_MAPPER.readValue(architectureAsString, ArchitectureDataStructure.class);
    }
}
