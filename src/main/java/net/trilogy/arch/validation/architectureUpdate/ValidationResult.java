package net.trilogy.arch.validation.architectureUpdate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationResult {
    private final LinkedHashSet<ValidationError> errors;

    public ValidationResult(Collection<ValidationError> errors) {
        this.errors = new LinkedHashSet<>(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean isValid(ValidationStage stage) {
        return errors.stream().noneMatch(error -> error.getValidationErrorType().getStage() == stage);
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<ValidationError> getErrors(ValidationStage stage) {
        return errors.stream()
                .filter(error -> error.getValidationErrorType().getStage().equals(stage))
                .collect(Collectors.toList());
    }
}
