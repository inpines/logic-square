package org.dotspace.oofp.utils.dsl.pipeline;

import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.functional.Functions;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(staticName = "steps")
public class BehaviorPipeline<T> {

    private final List<BehaviorStep<T>> steps = new ArrayList<>();

    public BehaviorPipeline<T> with(BehaviorStep<T> step) {
        this.steps.add(step);
        return this;
    }

    /**
     * 執行組裝好的流程。
     * 若中途遇到 invalid，則流程會停止。
     */
    public <R> Validation<Violations, R> apply(@NonNull T input, @NonNull Function<StepContext<T>, R> resultApplier) {
        StepContext<T> context = StepContext.<T>builder()
                .withPayload(input)
                .withViolations(Violations.empty())
                .build();

        for (BehaviorStep<T> step : steps) {
            Validation<Violations, StepContext<T>> result = step.execute(context);

            if (result.isInvalid()) {
                result.error().match(context::addViolation);
                return result.error()
                        .map(Validation::<Violations, R>invalid)
                        .orElseThrow();
            }

            context = result.get()
                    .orElseThrow();

            boolean aborted = result.map(StepContext::isAborted)
                    .fold(violations -> false, Boolean::booleanValue);

            if (aborted) {
                break;
            }

        }

        return Validation.valid(resultApplier.apply(context));
    }

    public <R> Validation<Violations, R> applyCorrectErrors(
            @NonNull T input, @NonNull Function<StepContext<T>, R> resultApplier) {

        StepContext<T> context = StepContext.<T>builder()
                .withPayload(input)
                .withViolations(Violations.empty())
                .build();

        for (BehaviorStep<T> step : steps) {
            Validation<Violations, StepContext<T>> result = step.execute(context);

            context = result.fold(
                    context::mergeViolations,     // invalid：累積 violations，不中斷
                    Functions.self()              // valid：承接新 context
            );

            // aborted：以「目前 context」判定（因為 invalid 時 result 沒 context）
            if (context.isAborted()) {
                break;
            }

        }

        if (!context.getViolations().isEmpty()) {
            return Validation.invalid(context.getViolations());
        }

        return Validation.valid(resultApplier.apply(context));

    }
}
