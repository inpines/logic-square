package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;

import org.dotspace.oofp.utils.eip.routekey.RouteKey;
import lombok.experimental.UtilityClass;

import java.util.function.Function;

@UtilityClass
public class SelectorOps {

    public <T> BehaviorStep<T> select(
            Function<StepContext<T>, RouteKey> decider, Function<RouteKey, BehaviorStep<T>> routeResolver) {
        return sc -> {
            var key = decider.apply(sc);
            var step = routeResolver.apply(key);
            return step.execute(sc);
        };
    }
}
