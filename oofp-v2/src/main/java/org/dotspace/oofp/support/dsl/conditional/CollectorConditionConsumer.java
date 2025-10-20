package org.dotspace.oofp.support.dsl.conditional;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CollectorConditionConsumer<T> implements ConditionConsumer<T> {

    @NonNull
    private final ConditionPredicate<T> predicate;

    @Getter
    private final List<T> collected = new ArrayList<>();

    @Override
    public void accept(T t) {
        Optional.ofNullable(t)
                .filter(predicate)
                .ifPresent(collected::add);
    }

}
