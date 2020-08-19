package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.annotators.ArchitectureUpdateAnnotator;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.c4.Entity;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String REGEX = "(\\n\\s*-\\s['\\\"]?component-id['\\\"]?:\\s+['\\\"]?(\\d+)['\\\"]?).*((^\\n)*\\n)";

    public AuAnnotateCommand(FilesFacade filesFacade) {
        this.filesFacade = filesFacade;
        architectureDataStructureObjectMapper = new ArchitectureDataStructureObjectMapper();
        architectureUpdateReader = new ArchitectureUpdateReader(filesFacade);
    }

    @Override
    public Integer call() {
        logArgs();
        var regexToGetComponentReferences = Pattern.compile(REGEX);

        String auAsString = null;
        var auAsStringOpt = loadAuString(architectureUpdateDirectory);
        if (auAsStringOpt.isEmpty()) return 2;
        else auAsString = auAsStringOpt.get();

        var au = loadAu(auAsString);
        if (au.isEmpty()) return 2;

        final Matcher matcher = regexToGetComponentReferences.matcher(auAsString);

        final var architecture = loadArchitectureOrPrintError("Unable to load Architecture product-architecture.yml.");
        if (architecture.isEmpty()) return 2;

        ArchitectureDataStructure dataStructure = architecture.get();

        Set<Entity> componentsToValidate = new ArchitectureUpdateAnnotator().getComponentsToValidate(dataStructure, au.get());
        if (componentsToValidate.isEmpty()) {
            printError("No valid components to annotate.");
            return 1;
        }

        try {
            new ArchitectureUpdateAnnotator()
                    .annotateC4Paths(
                            dataStructure,
                            auAsString,
                            architectureUpdateDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME),
                            filesFacade);
        } catch (Exception e) {
            printError("Unable to write annotations to Architecture Update.", e);
            return 2;
        }

        print("AU has been annotated with component paths.");
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

    private Optional<ArchitectureUpdate> loadAu(String auAsString) {
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
