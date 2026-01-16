package org.dotspace.oofp.utils.eip.flow;

import org.dotspace.oofp.model.dto.eip.InboundEnvelope;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

@FunctionalInterface
public interface InboundFlow<T> {

    Validation<Violations, StepContext<T>> from(InboundEnvelope<T> envelope);

    default InboundFlow<T> andThen(BehaviorStep<T> step) {
        return env -> from(env).flatMap(step::execute);
    }

}
