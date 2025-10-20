package org.dotspace.oofp.support.dsl.step;

import lombok.experimental.UtilityClass;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.violation.joinable.Violations;

import java.util.function.Consumer;

@UtilityClass
public class LoggerSteps {

    public static <T> BehaviorStep<T> tagAndLogOnError(
            String stepName, BehaviorStep<T> step, Consumer<Violations> logger) {
        return step.peekOnError(violations -> {
            violations.tagStep(stepName);
            logger.accept(violations);
        });
    }

}
