package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateObjectMapper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.annotators.ArchitectureUpdateAnnotator;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "annotate", description = "Annotates the architecture update with comments detailing the full paths of all components referenced by ID. Makes the AU easier to read.", mixinStandardHelpOptions = true)
public class AuAnnotateCommand implements Callable<Integer>, LoadArchitectureMixin, DisplaysOutputMixin, DisplaysErrorMixin {

    @Parameters(index = "0", description = "Directory name of architecture update to annotate")
    private File architectureUpdateDirectory;

    @Getter
    @Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @Getter
    @Spec
    private CommandSpec spec;

    @Getter
    private final FilesFacade filesFacade;

    @Getter
    private final ArchitectureDataStructureObjectMapper architectureDataStructureObjectMapper;
    private final ArchitectureUpdateReader architectureUpdateReader;

    public AuAnnotateCommand(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
        architectureDataStructureObjectMapper = new ArchitectureDataStructureObjectMapper();
        architectureUpdateReader = new ArchitectureUpdateReader(filesFacade);
    }

    @Override
    public Integer call() {
        logArgs();

        ArchitectureUpdateAnnotator annotator = new ArchitectureUpdateAnnotator();

        var au = loadAu(architectureUpdateDirectory);
        if (au.isEmpty()) return 2;

        try {
            ArchitectureUpdate tddContentAnnotatedAu = annotator.annotateTddContentFiles(au.get());
            String s = new ArchitectureUpdateObjectMapper().writeValueAsString(tddContentAnnotatedAu);
            filesFacade.writeString(architectureUpdateDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME), s);
        } catch (Exception e) {
            printError("Unable to write TDD content files annotations to Architecture Update.", e);
            return 2;
        }

        var auAsString = loadAuString(architectureUpdateDirectory);
        if (auAsString.isEmpty()) return 2;

        final var architecture = loadArchitectureOrPrintError("Unable to load Architecture product-architecture.yml.");
        if (architecture.isEmpty()) return 2;

        if (annotator.isComponentsEmpty(architecture.get(), au.get())) {
            printError("No valid components to annotate.");
            return 1;
        }

        try {
            String c4PathAnnotatedAu = annotator.annotateC4Paths(architecture.get(), auAsString.get());
            filesFacade.writeString(architectureUpdateDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME), c4PathAnnotatedAu);
        } catch (Exception e) {
            printError("Unable to write C4 path annotations to Architecture Update.", e);
            return 2;
        }

        print("AU has been annotated.");

        return 0;
    }

    private Optional<String> loadAuString(File architectureUpdateDirectory) {
        String auAsString = null;
        try {
            auAsString = filesFacade.readString(architectureUpdateDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME));
        } catch (Exception e) {
            printError("Unable to load Architecture Update.", e);
            return Optional.empty();
        }

        return Optional.of(auAsString);
    }

    private Optional<ArchitectureUpdate> loadAu(File architectureUpdateDirectory) {
        ArchitectureUpdate au = null;
        try {
            au = architectureUpdateReader.load(architectureUpdateDirectory.toPath());
        } catch (Exception e) {
            printError("Unable to load Architecture Update.", e);
            return Optional.empty();
        }

        return Optional.of(au);
    }
}
