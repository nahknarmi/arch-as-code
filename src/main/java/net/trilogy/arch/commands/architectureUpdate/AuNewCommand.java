package net.trilogy.arch.commands.architectureUpdate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.google.GoogleDocsApiInterface;
import net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory;
import net.trilogy.arch.adapter.google.GoogleDocumentReader;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.ARCHITECTURE_UPDATE_YML;

@Command(name = "new", mixinStandardHelpOptions = true, description = "Create a new architecture update.")
@RequiredArgsConstructor
public class AuNewCommand implements Callable<Integer>, DisplaysErrorMixin, DisplaysOutputMixin {
    private final GoogleDocsAuthorizedApiFactory googleDocsApiFactory;
    private final FilesFacade filesFacade;
    private final GitInterface gitInterface;

    @Parameters(index = "0", description = "Name for new architecture update")
    private String name;

    @Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @Option(names = {"-p", "--p1-url"}, description = "Url to P1 Google Document, used to import decisions and other data")
    private String p1GoogleDocUrl;

    @Getter
    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        logArgs();
        if (!checkBranchNameEquals(name)) return 1;

        var auFile = getNewAuFilePath(name);
        if (auFile.isEmpty()) return 1;

        var au = loadAu(name);
        if (au.isEmpty()) return 1;

        if (!writeAu(auFile.get(), au.get())) return 1;

        print(String.format("AU created - %s", auFile.get().toPath()));
        return 0;
    }

    private Optional<ArchitectureUpdate> loadAu(String name) {
        if (p1GoogleDocUrl != null) {
            return loadFromP1();
        } else {
            return Optional.of(ArchitectureUpdate.builderPreFilledWithBlanks().name(name).build());
        }
    }

    private boolean writeAu(File auFile, ArchitectureUpdate au) {
        try {
            filesFacade.writeString(auFile.toPath(), YAML_OBJECT_MAPPER.writeValueAsString(au));
            return true;
        } catch (Exception e) {
            printError("Unable to write AU file.", e);
            return false;
        }
    }

    private Optional<ArchitectureUpdate> loadFromP1() {
        try {
            GoogleDocsApiInterface authorizedDocsApi = googleDocsApiFactory.getAuthorizedDocsApi(productArchitectureDirectory);
            return Optional.of(new GoogleDocumentReader(authorizedDocsApi).load(p1GoogleDocUrl));
        } catch (Exception e) {
            String configPath = productArchitectureDirectory.toPath().resolve(".arch-as-code").toAbsolutePath().toString();
            printError("ERROR: Unable to initialize Google Docs API. Does configuration " + configPath + " exist?", e);
            return Optional.empty();
        }
    }

    private Optional<File> getNewAuFilePath(String name) {
        File baseAuFolder = productArchitectureDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATES_ROOT_FOLDER).toFile();

        if (!Files.exists(baseAuFolder.toPath())) {
            try {
                filesFacade.createDirectory(baseAuFolder.toPath());
            } catch (Exception e) {
                printError("Unable to create architecture-updates directory.", e);
                return Optional.empty();
            }
        }

        Path auFolder = baseAuFolder.toPath().resolve(name);
        if (!auFolder.toFile().isDirectory()) {
            try {
                filesFacade.createDirectory(auFolder);
            } catch (Exception e) {
                printError(String.format("Unable to create %s directory.", auFolder.toString()), e);
                return Optional.empty();
            }
        }

        File auFile = auFolder.resolve(ARCHITECTURE_UPDATE_YML).toFile();
        if (auFile.isFile()) {
            printError(String.format("AU %s already exists. Try a different name.", auFile.getAbsolutePath()));
            return Optional.empty();
        }

        return Optional.of(auFile);
    }

    private boolean checkBranchNameEquals(String str) {
        try {
            String branch = gitInterface.getBranch(productArchitectureDirectory);
            if (branch.equals(str)) return true;
            printError(
                    "ERROR: AU must be created in git branch of same name." +
                            "\nCurrent git branch: '" + branch + "'" +
                            "\nAu name: '" + str + "'"
            );
            return false;
        } catch (Exception e) {
            printError("ERROR: Unable to check git branch", e);
            return false;
        }
    }
}
