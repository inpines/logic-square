package org.dotspace.oofp.support.dsl.conditional;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class MappedCollectorConditionConsumer<T, R> implements ConditionConsumer<T> {

    @NonNull
    private final ConditionPredicate<T> predicate;

    @NonNull
    private final Function<T, R> mapper;

    @Getter
    private final List<R> collected = new ArrayList<>();

    @Override
    public void accept(T t) {
        Optional.ofNullable(t)
                .filter(predicate)
                .map(mapper)
                .ifPresent(collected::add);
    }

}
