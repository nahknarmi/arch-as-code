package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
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
import java.util.stream.Collectors;

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
        ArchitectureUpdate au = null;
        try {
            auAsString = filesFacade.readString(architectureUpdateDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME));
            au = architectureUpdateReader.load(architectureUpdateDirectory.toPath());
        } catch (Exception e) {
            printError(e, "Unable to load Architecture Update.");
            return 2;
        }

        final Matcher matcher = regexToGetComponentReferences.matcher(auAsString);

        final var architecture = loadArchitectureOrPrintError("Unable to load Architecture product-architecture.yml.");
        if (architecture.isEmpty()) return 2;

        ArchitectureDataStructure dataStructure = architecture.get();
        Set<Entity> collect = au.getTddContainersByComponent().stream()
                .map(tdd -> dataStructure
                        .getModel()
                        .findEntityById(tdd.getComponentId().getId())
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        if (collect.isEmpty()) {
            printError("No valid components to annotate.");
            return 1;
        }

        while (matcher.find()) {
            auAsString = matcher.replaceAll((res) ->
                    res.group(1) +
                            getComponentPathComment(res.group(2), architecture.get()) +
                            res.group(3)
            );
        }

        try {
            filesFacade.writeString(architectureUpdateDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME), auAsString);
        } catch (Exception e) {
            printError(e, "Unable to write annotations to Architecture Update.");
            return 2;
        }

        print("AU has been annotated with component paths.");
        return 0;
    }

    private void printError(Exception e, String s) {
        printError(s +
                "\nError: " + e + "\nCause: " + e.getCause());
    }

    private String getComponentPathComment(String id, ArchitectureDataStructure architecture) {
        try {
            return "  # " + architecture.getModel().findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id)).getPath().getPath();
        } catch (Exception ignored) {
            return "";
        }
    }
}
