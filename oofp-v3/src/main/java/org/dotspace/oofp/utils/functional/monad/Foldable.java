package org.dotspace.oofp.utils.functional.monad;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface Foldable<T> {

    default <R> R fold(R identity, BiFunction<R, ? super T, R> reducer) {
        return stream().reduce(identity, reducer, (prior, later) -> later);
    }

    default Optional<T> reduce(BinaryOperator<T> combiner) {
        return stream().reduce(combiner);
    }

    default <R, A> R collect(Collector<T, A, R> collector) {
        return stream().collect(collector);
    }

    Stream<T> stream();
}
