package org.dotspace.oofp.support.validation.policy;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.dotspace.oofp.support.validation.ValidationsContext;

public class PluralMemberValidationPolicy<T, I> extends BaseValidationPolicy<T> {

    private Function<T, Collection<I>> reader;
    
    private BiPredicate<I, ValidationsContext<T>> validator;

    public static <T, I> PluralMemberValidationPolicy<T, I> each(
        Function<T, Collection<I>> reader) {
        return new PluralMemberValidationPolicy<>(reader);
    }

    private PluralMemberValidationPolicy(Function<T, Collection<I>> reader) {
        this.reader = reader;
    }
    
    public PluralMemberValidationPolicy<T, I> with(
        BiPredicate<I, ValidationsContext<T>> validator) {
            this.validator = validator;
            return this;
    }

   @Override
    public boolean validate(T model, ValidationsContext<T> ctx) {
        return Optional.ofNullable(model)
        .map(reader)
        .map(collection -> collection.stream().allMatch(
            item -> validator.test(item, ctx)))
        .isPresent();
    }

}