package org.dotspace.v1.validation.policy;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.dotspace.v1.validation.ValidationsContext;

public class SingularMemberValidationPolicy<T, I> extends BaseValidationPolicy<T> {
    
    private Function<T, I> reader;
    private BiPredicate<I, ValidationsContext<T>> validator;

    public static <T, I> SingularMemberValidationPolicy<T, I> select(Function<T, I> reader) {
        return new SingularMemberValidationPolicy<>(reader);
    }
    
    protected SingularMemberValidationPolicy(Function<T, I> reader) {
        this.reader = reader;
    }

    public SingularMemberValidationPolicy<T, I> with(
        BiPredicate<I, ValidationsContext<T>> validator) {
            this.validator = validator;
            return this;
    }

    @Override
    public boolean validate(T model, ValidationsContext<T> ctx) {
        return Optional.ofNullable(model)
        .map(reader)
        .filter(mdl -> validator.test(mdl, ctx))
        .isPresent();
    }

}