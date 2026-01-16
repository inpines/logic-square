package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.functional.monad.Maybe;

import java.util.Map;

public final class QueryTable<T> {

    private final Map<String, BehaviorStep<T>> steps;

    public QueryTable(Map<String, BehaviorStep<T>> steps) {
        this.steps = steps;
    }

    public Maybe<BehaviorStep<T>> find(String name) {
        return Maybe.given(steps.get(name));
    }

}
