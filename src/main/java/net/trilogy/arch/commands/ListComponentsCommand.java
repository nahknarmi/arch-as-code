package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.Entity;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@CommandLine.Command(name = "list-components", mixinStandardHelpOptions = true, description = "Outputs a CSV formatted list of components and their IDs, which are present in the architecture.")
public class ListComponentsCommand implements Callable<Integer>, LoadArchitectureMixin, DisplaysOutputMixin {

    @CommandLine.Option(names = {"-s", "--search"}, description = "Search string to be part of name or description to find matching components.")
    private String searchString;

    @Getter
    @Spec
    private CommandSpec spec;

    @Getter
    @CommandLine.Parameters(index = "0", description = "Directory containing the product architecture")
    private File productArchitectureDirectory;

    @Getter
    private final ArchitectureDataStructureObjectMapper architectureDataStructureObjectMapper;

    @Getter
    private final FilesFacade filesFacade;

    public ListComponentsCommand( FilesFacade filesFacade ) {
        this.filesFacade = filesFacade;
        this.architectureDataStructureObjectMapper = new ArchitectureDataStructureObjectMapper();
    }

    @Override
    public Integer call() {
        logArgs();
        final var arch = loadArchitectureOrPrintError("Unable to load architecture");
        if(arch.isEmpty()) return 1;

        outputComponents(arch.get());

        return 0;
    }

    private void outputComponents(ArchitectureDataStructure arch) {
        String toOutput = arch.getModel()
                            .getComponents()
                            .stream()
                            .sorted(comparing(Entity::getId))
                            .filter( c -> searchString == null || searchComponent(c))
                            .map(component -> 
                                    "\n" +
                                    component.getId() + ", " + 
                                    component.getName() + ", " + 
                                    (component.getPath() == null ? "" : component.getPath().getPath())
                            )
                            .collect(Collectors.joining());

        print("ID, Name, Path" + toOutput);
    }

    private boolean searchComponent(C4Component component) {
        String lowerCase = searchString.toLowerCase();
        String name = component.getName();
        String description = component.getDescription();
        return  (name != null && name.toLowerCase().contains(lowerCase)) || (description != null && description.toLowerCase().contains(lowerCase));
    }
}
