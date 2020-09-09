package net.trilogy.arch.adapter.architectureDataStructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.view.ViewSet;
import lombok.experimental.UtilityClass;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.services.Base64Converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.adapter.structurizr.StructurizrViewsMapper.STRUCTURIZR_VIEWS_JSON;

@UtilityClass
public class ArchitectureDataStructureWriter {
    public static File exportArchitectureDataStructure(ArchitectureDataStructure dataStructure, FilesFacade files) throws IOException {
        final var tempFile = files.createTempFile("arch-as-code", ".yml");

        return exportArchitectureDataStructure(dataStructure, tempFile, files);
    }

    public static File exportArchitectureDataStructure(ArchitectureDataStructure dataStructure, File writeFile, FilesFacade files) throws IOException {
        files.writeString(writeFile.toPath(), YAML_OBJECT_MAPPER.writeValueAsString(dataStructure));

        final var writePath = Path.of(writeFile.getParent()).resolve("documentation");
        if (!writePath.toFile().exists())
            files.createDirectory(writePath);

        writeDocumentation(dataStructure, writePath, files);
        writeDocumentationImages(dataStructure, writePath, files);

        return writeFile;
    }

    public static Path writeViews(ViewSet structurizrViews, Path parentPath, FilesFacade files) throws IOException {
        final var viewsWritePath = parentPath.resolve(STRUCTURIZR_VIEWS_JSON);
        if (structurizrViews != null) {
            ObjectMapper mapper = new ObjectMapper();
            files.writeString(viewsWritePath, mapper.writeValueAsString(structurizrViews));
        }

        return viewsWritePath;
    }

    private static void writeDocumentation(ArchitectureDataStructure dataStructure, Path documentation, FilesFacade files) throws IOException {
        for (final var doc : dataStructure.getDocumentation()) {
            final var docFile = documentation.resolve(doc.getFileName()).toFile();
            files.writeString(docFile.toPath(), doc.getContent());
        }
    }

    private static void writeDocumentationImages(ArchitectureDataStructure dataStructure, Path writePath, FilesFacade files) throws IOException {
        for (final var image : dataStructure.getDocumentationImages()) {
            Base64Converter.toFile(files, image.getContent(), writePath.resolve(image.getName()));
        }
    }
}
