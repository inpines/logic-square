package org.dotspace.oofp.utils.builder.operation;

import org.dotspace.oofp.utils.functional.Predicates;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class WriteConditions {

    public <T, V> Predicate<T> present(V value) {
        return t -> matches(t, Predicates.isPresent(), x -> value);
    }

    public <T, C> Predicate<T> matchContext(
            Predicate<C> predicate, C ctx) {
        return t -> matches(t, predicate, inst -> ctx);
    }

    public <T, V> Predicate<T> matchBy(
            Predicate<V> predicate, Function<T, V> reader) {
        return t -> matches(t, predicate, reader);
    }

    private <T, C> boolean matches(T t, Predicate<C> predicate, Function<T, C> reader) {
        if (null == predicate) {
            return true;
        }

        C cond = Optional.ofNullable(t)
                .map(reader)
                .orElse(null);

        return predicate.test(cond);
    }

}
