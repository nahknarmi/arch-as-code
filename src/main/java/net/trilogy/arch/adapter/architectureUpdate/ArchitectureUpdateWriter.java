package net.trilogy.arch.adapter.architectureUpdate;

import lombok.experimental.UtilityClass;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.nio.file.Path;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate.ARCHITECTURE_UPDATE_YML;

@UtilityClass
public class ArchitectureUpdateWriter {
    public static void exportArchitectureUpdate(YamlArchitectureUpdate au, Path path, FilesFacade files) throws IOException {
        final var auPath = path.resolve(ARCHITECTURE_UPDATE_YML);

        files.writeString(auPath, YAML_OBJECT_MAPPER.writeValueAsString(au));
    }
}
