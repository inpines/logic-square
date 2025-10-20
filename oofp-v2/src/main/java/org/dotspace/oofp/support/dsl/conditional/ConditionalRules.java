package org.dotspace.oofp.support.dsl.conditional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConditionalRules<T> {

    private final List<ConditionConsumer<T>> conditionConsumers = new ArrayList<>();

    public ConditionalRules<T> with(ConditionConsumer<T> conditionConsumer) {
        conditionConsumers.add(conditionConsumer);
        return this;
    }

    public void execute(T request) {
        conditionConsumers.forEach(conditionConsumer -> conditionConsumer.accept(request));
    }

    public ConditionalRules<T> merge(ConditionalRules<T> other) {
        this.conditionConsumers.addAll(other.conditionConsumers);
        return this;
    }

}
