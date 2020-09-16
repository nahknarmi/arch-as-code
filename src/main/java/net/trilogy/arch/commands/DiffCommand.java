package net.trilogy.arch.commands;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.graphviz.GraphvizInterface;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureFromGitMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.services.DiffToDotCalculator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import static net.trilogy.arch.services.ArchitectureDiffCalculator.architectureDiff;

@Command(name = "diff", mixinStandardHelpOptions = true, description = "Display the diff between product architecture in current branch and specified branch.")
public class DiffCommand
        implements Callable<Integer>,
        LoadArchitectureMixin,
        LoadArchitectureFromGitMixin,
        DisplaysOutputMixin,
        DisplaysErrorMixin {
    @Getter
    private final GitInterface gitInterface;
    @Getter
    private final FilesFacade filesFacade;
    private final GraphvizInterface graphvizInterface;

    private final ArchitectureUpdateReader architectureUpdateReader;

    @Getter
    @Spec
    private CommandSpec spec;

    @Getter
    @Parameters(index = "0", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @Option(names = {"-b", "--branch-of-diff-architecture"}, description = "Name of git branch to compare against current architecture. Usually 'master'. Also can be a git commit or tag.", required = true)
    private String baseBranch;

    @Getter
    @Option(names = {"-u", "--architecture-update"}, description = "Name of the architecture update folder, to use it fo show related TDDs.")
    private File architectureUpdateDirectory;

    @Option(names = {"-o", "--output-directory"}, description = "New directory in which svg files will be created.", required = true)
    private File outputDirectory;

    public DiffCommand(FilesFacade filesFacade, GitInterface gitInterface, GraphvizInterface graphvizInterface, ArchitectureUpdateReader architectureUpdateReader) {
        this.filesFacade = filesFacade;
        this.gitInterface = gitInterface;
        this.graphvizInterface = graphvizInterface;
        this.architectureUpdateReader = architectureUpdateReader;
    }

    @VisibleForTesting
    static void diffConnectToTdds(Set<Diff> componentLevelDiffs, ArchitectureUpdate architectureUpdate) {
        final var containers = architectureUpdate.getTddContainersByComponent();
        componentLevelDiffs.forEach(diff -> {
            final var componentId = diff.getElement().getId();
            containers.stream()
                    .filter(it -> it.getComponentId().getId().equalsIgnoreCase(componentId))
                    // TODO: Wonky algorithm -- if there are multiple, only update one of them?
                    .findFirst()
                    .ifPresent(it -> diff.getElement().setRelatedTdds(it.getTdds()));
        });
    }

    @Override
    public Integer call() {
        logArgs();
        final var currentArch = loadArchitectureOrPrintError("Unable to load architecture file");
        if (currentArch.isEmpty()) return 1;

        final var beforeArch = loadArchitectureFromGitOrPrintError(baseBranch, "Unable to load '" + baseBranch + "' branch architecture");
        if (beforeArch.isEmpty()) return 1;

        final var architectureUpdate = loadArchitectureUpdate();

        final Path outputDir;
        try {
            outputDir = filesFacade.createDirectory(outputDirectory.toPath());
        } catch (Exception e) {
            printError("Unable to create output directory", e);
            return 1;
        }

        final var diffSet = architectureDiff(beforeArch.get(), currentArch.get());
        final var systemLevelDiffs = diffSet.getSystemLevelDiffs();

        var success = render(systemLevelDiffs, null, outputDir.resolve("system-context-diagram.svg"), "assets/");
        for (var system : systemLevelDiffs) {
            if (!success) return 1;
            final var systemId = system.getElement().getId();
            final var containerLevelDiffs = diffSet.getContainerLevelDiffs(systemId);
            if (containerLevelDiffs.size() == 0) continue;
            success = render(containerLevelDiffs, system, outputDir.resolve("assets/" + systemId + ".svg"), "");
            for (var container : containerLevelDiffs) {
                if (!success) return 1;
                final var containerId = container.getElement().getId();
                final var componentLevelDiffs = diffSet.getComponentLevelDiffs(containerId);
                architectureUpdate.ifPresent(it -> diffConnectToTdds(componentLevelDiffs, it));

                if (componentLevelDiffs.size() == 0) continue;
                success = render(componentLevelDiffs, container, outputDir.resolve("assets/" + containerId + ".svg"), "");
                for (var component : componentLevelDiffs) {
                    if (!success) return 1;
                    if (component.getElement().hasRelatedTdds()) {
                        final var componentId = component.getElement().getId();
                        success = renderTdds(component, outputDir.resolve("assets/" + componentId + ".svg"));
                    }
                }
            }
        }

        if (!success) return 1;

        print("SVG files created in " + outputDir.toAbsolutePath().toString());

        return 0;
    }

    Optional<ArchitectureUpdate> loadArchitectureUpdate() {
        if (getArchitectureUpdateDirectory() == null) return Optional.empty();

        try {
            return Optional.of(architectureUpdateReader.loadArchitectureUpdate(getArchitectureUpdateDirectory().toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private boolean renderTdds(Diff component, Path outputFile) {
        final var dotGraph = DiffToDotCalculator.toDot("diff", component);

        return createSvg(outputFile, dotGraph);
    }

    private boolean render(Set<Diff> diffs, Diff parentEntityDiff, Path outputFile, String linkPrefix) {
        final var dotGraph = DiffToDotCalculator.toDot("diff", diffs, parentEntityDiff, linkPrefix);

        return createSvg(outputFile, dotGraph);
    }

    private boolean createSvg(Path outputFile, String dotGraph) {
        final var name = outputFile.getFileName().toString().replaceAll(".svg", ".gv");

        try {
            graphvizInterface.render(dotGraph, outputFile);
            filesFacade.writeString(outputFile.getParent().resolve(name), dotGraph);
            return true;
        } catch (Exception e) {
            printError("Unable to render SVG", e);
            return false;
        }
    }
}
