package org.dotspace.oofp.support.validation.policy;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.oofp.support.validation.ValidationsContext;

public class SingularMemberValidationPolicy<T, I> extends BaseValidationPolicy<T> {
    
    private Function<T, I> reader;
    private Predicate<I> validator;
    private BiConsumer<I, ValidationsContext<T>> violationWriter;

    public static <T, I> SingularMemberValidationPolicy<T, I> select(Function<T, I> reader) {
        return new SingularMemberValidationPolicy<>(reader);
    }
    
    protected SingularMemberValidationPolicy(Function<T, I> reader) {
        this.reader = reader;
    }

    public SingularMemberValidationPolicy<T, I> with(
        Predicate<I> validator) {
            this.validator = validator;
            return this;
    }

    public SingularMemberValidationPolicy<T, I> ofViolation(BiConsumer<I, ValidationsContext<T>> violationWriter) {
    	this.violationWriter = violationWriter;
    	return this;
    }
    
    @Override
    public boolean validate(T model, ValidationsContext<T> ctx) {
        Optional<I> data = Optional.ofNullable(model)
        		.map(reader);
        
		return data
				.map(mdl -> validator.test(mdl))
				.filter(x -> Boolean.TRUE.equals(x))
				.orElseGet(() -> {
					violationWriter.accept(data.orElse(null), ctx);
					return false;
				});
    }

}