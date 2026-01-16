package org.dotspace.oofp.utils.functional.monad;

import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Maybe<T> implements Foldable<T> {

    private final T value;

    public static <T> Maybe<T> given(T value) {
        return new Maybe<>(value);
    }

    public static <T> Maybe<T> just(@NonNull T value) {
        return given(value);
    }

    public static <T> Maybe<T> empty() {
        return new Maybe<>(null);
    }

    public <R> Maybe<R> flatMap(Function<T, ? extends Maybe<R>> function) {

        Optional<Maybe<R>> monad = Optional.ofNullable(value)
                .flatMap(t -> Optional.ofNullable(function.apply(t))
                        .map(Casters.cast()));

        return monad.orElse(Maybe.empty());
    }

    public Maybe<T> filter(Predicate<? super T> predicate) {
        return Optional.ofNullable(value)
                .filter(predicate)
                .map(t -> this)
                .orElse(new Maybe<>(null));
    }

    public Optional<T> unwrap() {
        return Optional.ofNullable(value);
    }

    public T get() {
        return unwrap().orElse(null);
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    public T orElseThrow() {
        if (value == null) throw new NoSuchElementException("No value present");
        return value;
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value == null) throw exceptionSupplier.get();
        return value;
    }

    public Maybe<T> or(Maybe<T> other) {
        return value != null ? this : other;
    }

    public boolean isEmpty() {
        return unwrap().isEmpty();
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public <R> R fold(Function<T, R> ifPresent, Supplier<R> ifEmpty) {
        return unwrap().map(ifPresent)
                .orElseGet(ifEmpty);
    }

    public void match(Consumer<T> consumer) {
        unwrap().ifPresent(consumer);
    }

    public void match(Consumer<T> consumer, Runnable action) {
        unwrap().ifPresentOrElse(consumer, action);
    }

    @Override
    public Stream<T> stream() {
        return value == null ? Stream.empty() : Stream.of(value);
    }

    public <E> Maybe<E> map(Function<T, E> function) {
        return flatMap(t -> Maybe.given(function.apply(t)));
    }

    public Maybe<T> peek(Consumer<? super T> action) {
        unwrap().ifPresent(action);
        return this;
    }

    public <E extends Joinable<E>> Validation<E, T> toValidation(E violation) {
        return fold(Validation::valid, () -> Validation.invalid(violation));
    }

}
