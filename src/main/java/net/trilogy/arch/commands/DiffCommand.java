package net.trilogy.arch.commands;

import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.graphviz.GraphvizInterface;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureFromGitMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffSet;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.services.ArchitectureDiffCalculator;
import net.trilogy.arch.services.DiffToDotCalculator;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "diff", mixinStandardHelpOptions = true, description = "Display the diff between product architecture in current branch and specified branch.")
public class DiffCommand implements Callable<Integer>, LoadArchitectureMixin, LoadArchitectureFromGitMixin, DisplaysOutputMixin, DisplaysErrorMixin {
    @Getter
    private final GitInterface gitInterface;
    @Getter
    private final FilesFacade filesFacade;
    private final GraphvizInterface graphvizInterface;
    @Getter
    private final ArchitectureDataStructureObjectMapper architectureDataStructureObjectMapper;

    private final ArchitectureUpdateReader architectureUpdateReader;

    @Getter
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Getter
    @CommandLine.Parameters(index = "0", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @CommandLine.Option(names = {"-b", "--branch-of-diff-architecture"}, description = "Name of git branch to compare against current architecture. Usually 'master'. Also can be a git commit or tag.", required = true)
    private String baseBranch;

    @CommandLine.Option(names = {"-u", "--architecture-update"}, description = "Name of the architecture update folder, to use it fo show related TDDs.", required = false)
    private File architectureUpdateDirectory;

    @CommandLine.Option(names = {"-o", "--output-directory"}, description = "New directory in which svg files will be created.", required = true)
    private File outputDirectory;

    public DiffCommand(FilesFacade filesFacade, GitInterface gitInterface, GraphvizInterface graphvizInterface) {
        this.filesFacade = filesFacade;
        this.gitInterface = gitInterface;
        this.graphvizInterface = graphvizInterface;
        this.architectureDataStructureObjectMapper = new ArchitectureDataStructureObjectMapper();
        this.architectureUpdateReader = new ArchitectureUpdateReader(filesFacade);
    }

    @Override
    public Integer call() {
        logArgs();
        final var currentArch = loadArchitectureOrPrintError("Unable to load architecture file");
        if (currentArch.isEmpty()) return 1;

        final var beforeArch = loadArchitectureFromGitOrPrintError(baseBranch, "Unable to load '" + baseBranch + "' branch architecture");
        if (beforeArch.isEmpty()) return 1;

        Optional<ArchitectureUpdate> architectureUpdate = loadArchitectureUpdate();

        final Path outputDir;
        try {
            outputDir = filesFacade.createDirectory(outputDirectory.toPath());
        } catch (Exception e) {
            printError("Unable to create output directory", e);
            return 1;
        }

        final DiffSet diffSet = ArchitectureDiffCalculator.diff(beforeArch.get(), currentArch.get());
        Set<Diff> systemLevelDiffs = diffSet.getSystemLevelDiffs();

        var success = render(systemLevelDiffs, null, outputDir.resolve("system-context-diagram.svg"), "assets/");
        for (var system : systemLevelDiffs) {
            if (!success) return 1;
            String systemId = system.getElement().getId();
            Set<Diff> containerLevelDiffs = diffSet.getContainerLevelDiffs(systemId);
            if (containerLevelDiffs.size() == 0) continue;
            success = render(containerLevelDiffs, system, outputDir.resolve("assets/" + systemId + ".svg"), "");
            for (var container : containerLevelDiffs) {
                if (!success) return 1;
                String containerId = container.getElement().getId();
                Set<Diff> componentLevelDiffs = diffSet.getComponentLevelDiffs(containerId);
                connectToTdds(componentLevelDiffs, architectureUpdate);
                if (componentLevelDiffs.size() == 0) continue;
                success = render(componentLevelDiffs, container, outputDir.resolve("assets/" + containerId + ".svg"), "");
                for (var component : componentLevelDiffs) {
                    if (!success) return 1;
                    String[] relatedTo = component.getElement().getRelatedTo();
                    if (relatedTo != null && relatedTo.length > 0) {
                        String componentId = component.getElement().getId();
                        success = renderTdds(component, outputDir.resolve("assets/" + componentId + ".svg"));
                    }
                }
            }
        }
        if (!success) return 1;

        print("SVG files created in " + outputDir.toAbsolutePath().toString());

        return 0;
    }

    private void connectToTdds(Set<Diff> componentLevelDiffs, Optional<ArchitectureUpdate> architectureUpdate) {
        if (architectureUpdate.isPresent()) {
            List<TddContainerByComponent> tddContainersByComponent = architectureUpdate.get().getTddContainersByComponent();
            componentLevelDiffs.stream().forEach(c -> {
                String componentId = c.getElement().getId();
                Optional<TddContainerByComponent> tddC = tddContainersByComponent.stream().filter(tC -> tC.getComponentId().getId().equalsIgnoreCase(componentId)).findFirst();
                if (tddC.isPresent()) {
                    String[] relatedTdds = tddC.get().getTdds().entrySet().stream().map(e -> e.getKey() + " - " + e.getValue().getText()).toArray(String[]::new);
                    if (relatedTdds != null && relatedTdds.length > 0) {
                        c.getElement().setRelatedTo(relatedTdds);
                    }
                }
            });
        }
    }

    private Optional<ArchitectureUpdate> loadArchitectureUpdate() {
        if (architectureUpdateDirectory != null) {
            try {
                return Optional.of(architectureUpdateReader.load(architectureUpdateDirectory.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    private boolean renderTdds(Diff component, Path outputFile) {
        final String dotGraph = DiffToDotCalculator.toDot("diff", component);

        return createSvg(outputFile, dotGraph);
    }

    private boolean createSvg(Path outputFile, String dotGraph) {
        var name = outputFile.getFileName().toString().replaceAll(".svg", ".gv");

        try {
            graphvizInterface.render(dotGraph, outputFile);
            filesFacade.writeString(outputFile.getParent().resolve(name), dotGraph);
        } catch (Exception e) {
            printError("Unable to render SVG", e);
            return false;
        }

        return true;
    }

    private boolean render(Set<Diff> diffs, Diff parentEntityDiff, Path outputFile, String linkPrefix) {
        final String dotGraph = DiffToDotCalculator.toDot("diff", diffs, parentEntityDiff, linkPrefix);

        return createSvg(outputFile, dotGraph);
    }
}
