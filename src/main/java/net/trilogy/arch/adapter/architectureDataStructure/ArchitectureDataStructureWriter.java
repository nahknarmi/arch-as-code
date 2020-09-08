package net.trilogy.arch.adapter.architectureDataStructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.view.ViewSet;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.services.Base64Converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.adapter.structurizr.StructurizrViewsMapper.STRUCTURIZR_VIEWS_JSON;

@RequiredArgsConstructor
public class ArchitectureDataStructureWriter {
    private final FilesFacade filesFacade;

    public File export(ArchitectureDataStructure dataStructure) throws IOException {
        final var tempFile = filesFacade.createTempFile("arch-as-code", ".yml");

        return export(dataStructure, tempFile);
    }

    public File export(ArchitectureDataStructure dataStructure, File writeFile) throws IOException {
        filesFacade.writeString(writeFile.toPath(), YAML_OBJECT_MAPPER.writeValueAsString(dataStructure));

        final var writePath = Path.of(writeFile.getParent()).resolve("documentation");
        if (!writePath.toFile().exists())
            filesFacade.createDirectory(writePath);

        writeDocumentation(dataStructure, writePath);
        writeDocumentationImages(filesFacade, dataStructure, writePath);

        return writeFile;
    }

    public Path writeViews(ViewSet structurizrViews, Path parentPath) throws IOException {
        final var viewsWritePath = parentPath.resolve(STRUCTURIZR_VIEWS_JSON);
        if (structurizrViews != null) {
            ObjectMapper mapper = new ObjectMapper();
            filesFacade.writeString(viewsWritePath, mapper.writeValueAsString(structurizrViews));
        }

        return viewsWritePath;
    }

    private void writeDocumentation(ArchitectureDataStructure dataStructure, Path documentation) throws IOException {
        for (final var doc : dataStructure.getDocumentation()) {
            final var docFile = documentation.resolve(doc.getFileName()).toFile();
            filesFacade.writeString(docFile.toPath(), doc.getContent());
        }
    }

    private void writeDocumentationImages(FilesFacade filesFacade, ArchitectureDataStructure dataStructure, Path writePath) throws IOException {
        for (final var image : dataStructure.getDocumentationImages()) {
            Base64Converter.toFile(filesFacade, image.getContent(), writePath.resolve(image.getName()));
        }
    }
}
