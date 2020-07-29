package net.trilogy.arch.adapter.architectureYaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.view.ViewSet;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.DocumentationImage;
import net.trilogy.arch.domain.DocumentationSection;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.services.Base64Converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ArchitectureDataStructureWriter {
    private FilesFacade filesFacade;

    public ArchitectureDataStructureWriter(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
    }

    public File export(ArchitectureDataStructure dataStructure) throws IOException {
        File tempFile = filesFacade.createTempFile("arch-as-code", ".yml");

        return export(dataStructure, tempFile);
    }

    public File export(ArchitectureDataStructure dataStructure, File writeFile) throws IOException {
        ArchitectureDataStructureObjectMapper mapper = new ArchitectureDataStructureObjectMapper();
        filesFacade.writeString(writeFile.toPath(), mapper.writeValueAsString(dataStructure));

        final Path writePath = Path.of(writeFile.getParent()).resolve("documentation");
        if (!writePath.toFile().exists()) filesFacade.createDirectory(writePath);

        writeDocumentation(dataStructure, writePath);
        writeDocumentationImages(filesFacade, dataStructure, writePath);

        return writeFile;
    }

    public Path writeViews(ViewSet structurizrViews, Path parentPath) throws IOException {
        final Path viewsWritePath = parentPath.resolve("structurizrViews.json");
        if (structurizrViews != null) {
            ObjectMapper mapper = new ObjectMapper();
            filesFacade.writeString(viewsWritePath, mapper.writeValueAsString(structurizrViews));
        }
        return viewsWritePath;
    }

    private void writeDocumentation(ArchitectureDataStructure dataStructure, Path documentation) throws IOException {
        for (DocumentationSection doc : dataStructure.getDocumentation()) {
            final File docFile = documentation.resolve(doc.getFileName()).toFile();
            filesFacade.writeString(docFile.toPath(), doc.getContent());
        }
    }

    private void writeDocumentationImages(FilesFacade filesFacade, ArchitectureDataStructure dataStructure, Path writePath) throws IOException {
        for (DocumentationImage image : dataStructure.getDocumentationImages()) {
            Base64Converter.toFile(filesFacade, image.getContent(), writePath.resolve(image.getName()));
        }
    }
}
