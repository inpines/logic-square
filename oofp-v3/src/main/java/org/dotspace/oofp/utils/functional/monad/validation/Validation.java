package org.dotspace.oofp.utils.functional.monad.validation;

import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import lombok.NonNull;
import lombok.ToString;

import java.util.*;
import java.util.function.*;

public interface Validation<E extends Joinable<E>, T> {

    <R> Validation<E, R> map(Function<? super T, ? extends R> mapper);

    <R> Validation<E, R> flatMap(Function<? super T, Validation<E, R>> mapper);

    <U> U fold(Function<? super E, ? extends U> onInvalid,
               Function<? super T, ? extends U> onValid);

    boolean isValid();

    default boolean isInvalid() {
        return !isValid();
    }

    Maybe<T> get();

    // 副作用觀察：成功時呼叫
    default Validation<E, T> peek(Consumer<T> onSuccess) {
        if (isValid()) {
            get().match(onSuccess);
        }
        return this;
    }

    // 副作用觀察：錯誤時呼叫
    default Validation<E, T> peekError(Consumer<E> onError) {
        if (isInvalid()) {
            error().match(onError);
        }
        return this;
    }

    Maybe<E> error();

    static <E extends Joinable<E>, T> Validation<E, T> valid(T value) {
        return new Valid<>(value);
    }

    static <E extends Joinable<E>, T> Validation<E, T> invalid(E errors) {
        return new Invalid<>(errors);
    }

    static <E extends Joinable<E>, A, B, R> Validation<E, R> merge(
            Validation<E, A> va, Validation<E, B> vb,
            BiFunction<A, B, R> combiner) {
        if (va.isValid() && vb.isValid()) {
            return Validation.valid(combiner.apply(
                    va.get().orElse(null), vb.get().orElse(null))
            );
        }

        Maybe<E> be = vb.error();
        E combinedError = va.error().fold(
                e -> be.fold(e::join, () -> e), // empty vb.error -> va.error
                be::orElseThrow // empty va.error()
        ); // 保證有錯

        return Validation.invalid(combinedError);
    }

    static <E extends Joinable<E>, T> Validation<E, Map<String, Object>> mergeAll(
            Map<String, Validation<E, T>> validations) {
        Map<String, Object> values = new HashMap<>();
        List<E> errors = new ArrayList<>();

        for (var entry : validations.entrySet()) {
            String key = entry.getKey();
            Validation<E, T> val = entry.getValue();

            if (val.isValid()) {
                val.get().match(data -> values.put(key, data));
            } else {
                val.error().match(errors::add);
            }
        }

        if (errors.isEmpty()) {
            return Validation.valid(values);
        }

        E merged = errors.stream()
                .reduce(Joinable::join)
                .orElseThrow();

        return Validation.invalid(merged);
    }

    default Validation<E, T> filter(Predicate<T> predicate, E onFailure) {
        return filter(predicate, () -> onFailure);
    }

    default Validation<E, T> filter(Predicate<T> predicate, Supplier<E> onFailureSupplier) {
        if (isInvalid()) {
            return this; // 保持原來的 Invalid
        }

        return get().filter(predicate)
                .<Validation<E, T>>map(Valid::new)
                .orElseGet(() -> new Invalid<>(onFailureSupplier.get()));

    }

    @ToString
    final class Valid<E extends Joinable<E>, T> implements Validation<E, T> {
        private final T value;

        public Valid(@NonNull T value) {
            this.value = Objects.requireNonNull(value);
        }

        public <R> Validation<E, R> map(Function<? super T, ? extends R> mapper) {
            return new Valid<>(mapper.apply(value));
        }

        public <R> Validation<E, R> flatMap(Function<? super T, Validation<E, R>> mapper) {
            return mapper.apply(value);
        }

        public <U> U fold(Function<? super E, ? extends U> onInvalid,
                          Function<? super T, ? extends U> onValid) {
            return onValid.apply(value);
        }

        public boolean isValid() {
            return true;
        }

        @Override
        public Maybe<T> get() {
            return Maybe.given(value);
        }

        @Override
        public Maybe<E> error() {
            return Maybe.empty();
        }

    }

    @ToString
    final class Invalid<E extends Joinable<E>, T> implements Validation<E, T> {
        private final E errors;

        public Invalid(E errors) {
            this.errors = errors;
        }

        public <R> Validation<E, R> map(Function<? super T, ? extends R> mapper) {
            return new Invalid<>(errors);
        }

        public <R> Validation<E, R> flatMap(Function<? super T, Validation<E, R>> mapper) {
            return new Invalid<>(errors);
        }

        public <U> U fold(Function<? super E, ? extends U> onInvalid,
                          Function<? super T, ? extends U> onValid) {
            return onInvalid.apply(errors);
        }

        public boolean isValid() {
            return false;
        }

        @Override
        public Maybe<T> get() {
            return Maybe.empty();
        }

        @Override
        public Maybe<E> error() {
            return Maybe.given(errors);
        }

    }
}
