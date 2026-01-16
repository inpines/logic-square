package org.dotspace.oofp.utils.functional.monad;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class Monads {

    public <T> Maybe<T> maybe(T value) {
        return Maybe.given(value);
    }

    public <T> Maybe<T> of(@NonNull T value) {
        return Maybe.just(value);
    }

    public <E> Sequence<E> sequence(Collection<E> values) {
        return Sequence.from(values);
    }

    public <E> Sequence<Pair<String, E>> sequence(Map<String, E> keyValues) {
        return Sequence.from(keyValues);
    }

    public <E> Sequence<E> sequence(E[] array) {
        return Sequence.of(array);
    }

    public <T, S> Sequence<T> unfold(S seed, Function<S, Maybe<Pair<T, S>>> generator) {
        return Sequence.unfold(seed, generator);
    }

    public <T> Task<T> task(Supplier<T> supplier) {
        return Task.action(supplier);
    }

    public <T> Task<T> future(CompletableFuture<T> future) {
        return Task.actionAsync(() -> future);
    }

}
