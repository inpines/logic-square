package org.dotspace.oofp.utils.dsl;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;

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
            Validation<Violations, StepContext<T>> result = step.apply(context);

            if (result.isInvalid()) {
                return result.error()
                        .map(Validation::<Violations, R>invalid)
                        .orElseThrow();
            }

            context = result.get()
                    .orElseThrow();
        }

        return Validation.valid(resultApplier.apply(context));
    }
}
