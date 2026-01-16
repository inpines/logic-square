package org.dotspace.oofp.utils.functional.monad.either;

import org.dotspace.oofp.utils.functional.monad.Foldable;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

public class Try<T> extends Either<Throwable, T> implements Foldable<T> {

    private Try(Throwable throwable, T value) {
        super(throwable, value);
    }

    public static <T> Try<T> attempt(Callable<T> action) {
        try {
            return new Try<>(null, action.call());
        } catch (Exception ex) {
            return new Try<>(ex, null);
        }
    }

    public static <T> Try<T> success(T value) {
        return new Try<>(null, value);
    }

    public static <T> Try<T> failed(Throwable error) {
        return new Try<>(error, null);
    }

    public boolean isSuccess() {
        return isRight();
    }

    public boolean isFailure() {
        return !isRight();
    }

    public T orElse(T value) {
        try {
            return getRight();
        } catch (Exception ex) {
            return value;
        }
    }

    @Override
    public Stream<T> stream() {
        return isSuccess() ? Stream.of(getRight()) : Stream.empty();
    }

    public <U> Try<U> flatMap(Function<T, ? extends Try<U>> fn) {
        return isSuccess() ? fn.apply(getRight()) : Try.failed(getLeft());
    }

    public <U> Try<U> map(Function<T, ? extends U> mapper) {
        return flatMap(v -> Try.attempt(() -> mapper.apply(v)));
    }

}