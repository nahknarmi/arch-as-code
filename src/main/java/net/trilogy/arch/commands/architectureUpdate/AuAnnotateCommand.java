package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
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

import static net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateWriter.exportArchitectureUpdate;
import static net.trilogy.arch.annotators.ArchitectureUpdateAnnotator.annotateC4Paths;
import static net.trilogy.arch.annotators.ArchitectureUpdateAnnotator.annotateTddContentFiles;

@Command(name = "annotate", description = "Annotates the architecture update with comments detailing the full paths of all components referenced by ID. Makes the AU easier to read.", mixinStandardHelpOptions = true)
public class AuAnnotateCommand implements Callable<Integer>, LoadArchitectureMixin, DisplaysOutputMixin, DisplaysErrorMixin {
    @Getter
    private final FilesFacade filesFacade;
    private final ArchitectureUpdateReader architectureUpdateReader;
    @Parameters(index = "0", description = "Directory name of architecture update to annotate")
    private File architectureUpdateDirectory;
    @Getter
    @Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @Getter
    @Spec
    private CommandSpec spec;

    public AuAnnotateCommand(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
        architectureUpdateReader = new ArchitectureUpdateReader(filesFacade);
    }

    public AuAnnotateCommand(FilesFacade filesFacade, CommandSpec spec, File architectureUpdateDirectory, File productArchitectureDirectory) {
        this(filesFacade);
        this.spec = spec;
        this.architectureUpdateDirectory = architectureUpdateDirectory;
        this.productArchitectureDirectory = productArchitectureDirectory;
    }

    @Override
    public Integer call() {
        logArgs();

        var au = loadAu(architectureUpdateDirectory);
        if (au.isEmpty()) return 2;

        final var architecture = loadArchitectureOrPrintError("Unable to load Architecture product-architecture.yml.");
        if (architecture.isEmpty()) return 2;

        if (ArchitectureUpdateAnnotator.isComponentsEmpty(architecture.get(), au.get())) {
            printError("No valid components to annotate.");
            return 1;
        }

        var architectureUpdate = annotateC4Paths(architecture.get(), au.get());
        architectureUpdate = annotateTddContentFiles(architectureUpdate);

        try {
            exportArchitectureUpdate(architectureUpdate, architectureUpdateDirectory.toPath(), filesFacade);
        } catch (Exception e) {
            printError("Unable to write annotated Architecture Update to yaml file.", e);
            return 2;
        }

        print("AU has been annotated.");

        return 0;
    }

    private Optional<ArchitectureUpdate> loadAu(File architectureUpdateDirectory) {
        try {
            ArchitectureUpdate au = architectureUpdateReader.loadArchitectureUpdate(architectureUpdateDirectory.toPath());
            return Optional.of(au);
        } catch (Exception e) {
            printError("Unable to load Architecture Update.", e);
            return Optional.empty();
        }
    }
}
