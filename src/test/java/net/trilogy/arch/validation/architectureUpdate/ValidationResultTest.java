package net.trilogy.arch.validation.architectureUpdate;

import net.trilogy.arch.domain.architectureUpdate.YamlDecision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class ValidationResultTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Test
    public void shouldBeValid() {
        ValidationResult result = new ValidationResult(Set.of());
        collector.checkThat(result.isValid(), is(true));
        collector.checkThat(result.isValid(ValidationStage.TDD), is(true));
        collector.checkThat(result.isValid(ValidationStage.STORY), is(true));
    }

    @Test
    public void shouldBeInvalid() {
        ValidationResult result = new ValidationResult(Set.of(
                ValidationError.forDecisionsMustHaveTdds(new DecisionId("ANY")),
                ValidationError.forMustHaveStories(new TddId("ANY"))
        ));

        collector.checkThat(result.isValid(), is(false));
        collector.checkThat(result.isValid(ValidationStage.TDD), is(false));
        collector.checkThat(result.isValid(ValidationStage.STORY), is(false));
    }

    @Test
    public void shouldBeInvalidForTddErrors() {
        ValidationResult result = new ValidationResult(Set.of(
                ValidationError.forDecisionsMustHaveTdds(new DecisionId("ANY"))
        ));

        collector.checkThat(result.isValid(), is(false));
        collector.checkThat(result.isValid(ValidationStage.TDD), is(false));

        collector.checkThat(result.isValid(ValidationStage.STORY), is(true));
    }

    @Test
    public void shouldBeInvalidForCapabilityErrors() {
        ValidationResult result = new ValidationResult(Set.of(
                ValidationError.forMustHaveStories(new TddId("ANY"))
        ));

        collector.checkThat(result.isValid(), is(false));
        collector.checkThat(result.isValid(ValidationStage.STORY), is(false));

        collector.checkThat(result.isValid(ValidationStage.TDD), is(true));
    }

    @Test
    public void shouldGetAllErrors() {
        Set<ValidationError> errors = Set.of(
                ValidationError.forMustHaveStories(TddId.blank()),
                ValidationError.forTddsMustBeValidReferences(DecisionId.blank(), TddId.blank()),
                ValidationError.forDecisionsMustHaveTdds(DecisionId.blank())
        );

        collector.checkThat(
                new ValidationResult(errors).getErrors(),
                containsInAnyOrder(errors.toArray())
        );
    }

    @Test
    public void shouldGetAllTddErrors() {
        Set<ValidationError> errors = Set.of(
                ValidationError.forMustHaveStories(TddId.blank()),
                ValidationError.forTddsMustBeValidReferences(DecisionId.blank(), TddId.blank()),
                ValidationError.forDecisionsMustHaveTdds(DecisionId.blank())
        );

        collector.checkThat(
                new ValidationResult(errors).getErrors(ValidationStage.TDD),
                containsInAnyOrder(
                        ValidationError.forTddsMustBeValidReferences(DecisionId.blank(), TddId.blank()),
                        ValidationError.forDecisionsMustHaveTdds(DecisionId.blank())
                )
        );
    }

    @Test
    public void shouldGetAllCapabilityErrors() {
        Set<ValidationError> errors = Set.of(
                ValidationError.forMustHaveStories(TddId.blank()),
                ValidationError.forTddsMustBeValidReferences(DecisionId.blank(), TddId.blank()),
                ValidationError.forDecisionsMustHaveTdds(DecisionId.blank())
        );

        collector.checkThat(
                new ValidationResult(errors).getErrors(ValidationStage.STORY),
                containsInAnyOrder(
                        ValidationError.forMustHaveStories(TddId.blank())
                )
        );
    }
}
