package org.dotspace.oofp.utils.functional.monad.either;

import org.dotspace.oofp.utils.functional.monad.Foldable;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import lombok.NonNull;

import java.util.function.Function;
import java.util.stream.Stream;

public class Either<L, R> implements Foldable<R> {

    private final L left;
    private final R right;

    public boolean isRight() {
        return Maybe.given(right)
                .isPresent();
    }

    protected Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(@NonNull L value) {
        return new Either<>(value, null);
    }

    public static <L, R> Either<L, R> right(@NonNull R value) {
        return new Either<>(null, value);
    }

    public boolean isLeft() {
        return !isRight();
    }

    public R getRight() {
        if (!isRight()) {
            throw new IllegalStateException("Not a right value");
        }
        return right;
    }

    public L getLeft() {
        if (isRight()) {
            throw new IllegalStateException("Not a left value");
        }
        return left;
    }

    @Override
    public Stream<R> stream() {
        return isRight() ? Stream.of(right) : Stream.empty();
    }

    public <T> T fold(Function<? super L, ? extends T> onLeft, Function<? super R, ? extends T> onRight) {
        return isRight() ? onRight.apply(right) : onLeft.apply(left);
    }
}
