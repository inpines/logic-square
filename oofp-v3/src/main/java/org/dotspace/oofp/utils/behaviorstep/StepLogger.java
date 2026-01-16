package org.dotspace.oofp.utils.behaviorstep;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

@UtilityClass
public class StepLogger {

    public static <T> BehaviorStep<T> tagAndLogOnError(
            String stepName, BehaviorStep<T> step, Consumer<Violations> logger) {
        return step.peekOnError(violations -> {
            violations.tagStep(stepName);
            logger.accept(violations);
        });
    }

}
