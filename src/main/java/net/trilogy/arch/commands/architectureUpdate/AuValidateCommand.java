package net.trilogy.arch.commands.architectureUpdate;

import com.networknt.schema.ValidationMessage;
import lombok.Getter;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.commands.mixin.DisplaysErrorMixin;
import net.trilogy.arch.commands.mixin.DisplaysOutputMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureFromGitMixin;
import net.trilogy.arch.commands.mixin.LoadArchitectureMixin;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.schema.SchemaValidator;
import net.trilogy.arch.validation.architectureUpdate.ArchitectureUpdateValidator;
import net.trilogy.arch.validation.architectureUpdate.ValidationError;
import net.trilogy.arch.validation.architectureUpdate.ValidationErrorType;
import net.trilogy.arch.validation.architectureUpdate.ValidationResult;
import net.trilogy.arch.validation.architectureUpdate.ValidationStage;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;
import static picocli.CommandLine.Spec;

@Command(name = "validate", description = "Validate Architecture Update", mixinStandardHelpOptions = true)
public class AuValidateCommand implements Callable<Integer>, LoadArchitectureFromGitMixin, LoadArchitectureMixin, DisplaysErrorMixin, DisplaysOutputMixin {
    @Getter
    private final ArchitectureDataStructureObjectMapper architectureDataStructureObjectMapper;
    @Getter
    private final FilesFacade filesFacade;
    @Getter
    private final GitInterface gitInterface;
    private final ArchitectureUpdateReader architectureUpdateReader;
    @Option(names = {"-b", "--branch-of-base-architecture"}, description = "Name of git branch from which this AU was branched. Used to validate changes. Usually 'master'. Also can be a commit or tag.", required = true)
    String baseBranch;
    @Option(names = {"-t", "--TDDs"}, description = "Run validation for TDDs only")
    boolean tddValidation;
    @Option(names = {"-s", "--stories"}, description = "Run validation for feature stories only")
    boolean capabilityValidation;
    @Parameters(index = "0", description = "Directory name of architecture update to validate")
    private File architectureUpdateDirectory;
    @Getter
    @Parameters(index = "1", description = "Product architecture root directory")
    private File productArchitectureDirectory;

    @Getter
    @Spec
    private CommandSpec spec;

    public AuValidateCommand(FilesFacade filesFacade, GitInterface gitInterface) {
        this.filesFacade = filesFacade;
        this.gitInterface = gitInterface;
        this.architectureDataStructureObjectMapper = new ArchitectureDataStructureObjectMapper();
        this.architectureUpdateReader = new ArchitectureUpdateReader(filesFacade);
    }

    @Override
    public Integer call() {
        logArgs();
        final var auBranchArch = loadArchitectureOrPrintError("Unable to load architecture file");
        if (auBranchArch.isEmpty()) return 1;

        final var baseBranchArch = loadArchitectureFromGitOrPrintError(baseBranch, "Unable to load '" + baseBranch + "' branch architecture");
        if (baseBranchArch.isEmpty()) return 1;

        final var au = loadAndValidateAu(architectureUpdateDirectory);
        if (au.isEmpty()) return 1;

        final ValidationResult validationResults = ArchitectureUpdateValidator.validate(
                au.get(), auBranchArch.get(), baseBranchArch.get()
        );

        final List<ValidationStage> stages = determineValidationStages(tddValidation, capabilityValidation);
        final boolean areAllStagesValid = stages.stream().allMatch(validationResults::isValid);

        if (!areAllStagesValid) {
            final List<ValidationError> errors = getErrorsOfStages(stages, validationResults);
            final String resultToDisplay = getPrettyStringOfErrors(errors, baseBranch);
            printError(resultToDisplay);
            return 1;
        }

        print("Success, no errors found.");
        return 0;
    }

    private Optional<ArchitectureUpdate> loadAndValidateAu(File auDirectory) {
        try {
            if (validateAuSchema(auDirectory.toPath().resolve(AuCommand.ARCHITECTURE_UPDATE_FILE_NAME).toFile())) {
                return Optional.of(architectureUpdateReader.load(auDirectory.toPath()));
            }
        } catch (final Exception e) {
            printError("Unable to load architecture update file", e);
        }

        return Optional.empty();
    }

    private boolean validateAuSchema(File auFile) throws FileNotFoundException {
        InputStream auInputStream = new FileInputStream(auFile);
        Set<ValidationMessage> validationMessages = new SchemaValidator().validateArchitectureUpdateDocument(auInputStream);

        if (validationMessages.isEmpty()) return true;

        printError("Architecture update schema validation failed.");
        validationMessages.forEach(m -> printError(m.getMessage()));

        return false;
    }

    private String getPrettyStringOfErrors(final List<ValidationError> errors, final String baseBranchName) {
        return getTypes(errors).stream()
                .map(type -> getErrorsOfType(type, errors))
                .map(it -> getPrettyStringOfErrorsInSingleType(it, baseBranchName))
                .collect(Collectors.joining())
                .trim();
    }

    private List<ValidationError> getErrorsOfStages(final List<ValidationStage> stages, final ValidationResult validationResults) {
        return stages.stream()
                .map(validationResults::getErrors)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<ValidationErrorType> getTypes(final List<ValidationError> errors) {
        return errors.stream()
                .map(ValidationError::getValidationErrorType)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<ValidationError> getErrorsOfType(final ValidationErrorType type, final List<ValidationError> allErrors) {
        return allErrors.stream()
                .filter(error -> error.getValidationErrorType() == type)
                .collect(Collectors.toList());
    }

    private String getPrettyStringOfErrorsInSingleType(final List<ValidationError> errors, final String baseBranchName) {
        return errors.get(0).getValidationErrorType() + ":" +
                errors.stream()
                        .map(error -> toString(error, baseBranchName))
                        .collect(Collectors.joining()) +
                "\n";
    }

    private String toString(ValidationError error, String baseBranchName) {
        var result = "\n    " + error.getDescription();
        if (error.getValidationErrorType() == ValidationErrorType.INVALID_DELETED_COMPONENT_REFERENCE)
            result += " (Checked architecture in \"" + baseBranchName + "\" branch.)";
        return result;
    }

    private List<ValidationStage> determineValidationStages(final boolean tddValidation, final boolean capabilityValidation) {
        if (tddValidation && capabilityValidation) {
            return List.of(ValidationStage.TDD, ValidationStage.STORY);
        }

        if (tddValidation) {
            return List.of(ValidationStage.TDD);
        }

        if (capabilityValidation) {
            return List.of(ValidationStage.STORY);
        }

        return List.of(ValidationStage.values());
    }
}
