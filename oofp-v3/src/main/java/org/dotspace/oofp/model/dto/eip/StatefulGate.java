package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.utils.dsl.BehaviorStep;

import java.util.Objects;
import java.util.stream.Stream;

public record StatefulGate<T>(
        BehaviorStep<T> claimsBinder,
        BehaviorStep<T> querySpecBinder,
        BehaviorStep<T> statusObserver,
        BehaviorStep<T> decider) {

    public BehaviorStep<T> step() {
        return BehaviorStep.chain(Stream.of(claimsBinder, querySpecBinder, statusObserver, decider)
                .filter(Objects::nonNull)
                .toList());
    }

}
