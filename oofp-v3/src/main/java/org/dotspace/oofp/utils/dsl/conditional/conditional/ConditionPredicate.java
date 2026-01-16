package org.dotspace.oofp.utils.dsl.conditional.conditional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ConditionPredicate<T> extends Predicate<T> {

    static <T> ConditionPredicate<T> when(Predicate<T> predicate) {
        return predicate::test;
    }

    default ConditionConsumer<T> then(Consumer<T> consumer) {
        return t -> Optional.ofNullable(t)
                .filter(this)
                .ifPresent(consumer);
    }

    default PartitionedCollectorConditionConsumer<T> partitioning() {
        return new PartitionedCollectorConditionConsumer<>(this);
    }

}
