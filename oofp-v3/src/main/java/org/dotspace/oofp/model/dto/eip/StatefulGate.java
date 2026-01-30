package org.dotspace.oofp.model.dto.eip;

import org.dotspace.oofp.utils.dsl.BehaviorStep;

import java.util.List;
import java.util.Objects;

public record StatefulGate<T>(
        List<BehaviorStep<T>> steps) {

    public BehaviorStep<T> step() {
        return BehaviorStep.chain(steps.stream()
                .filter(Objects::nonNull)
                .toList());
    }

}
