package org.dotspace.oofp.support.validation.policy;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.oofp.support.validation.ValidationsContext;

public class PluralMemberValidationPolicy<T, I> extends BaseValidationPolicy<T> {

    private Function<T, Collection<I>> reader;
    
    private Predicate<I> validator;

    private BiConsumer<I, ValidationsContext<T>> violationWriter;
    
    public static <T, I> PluralMemberValidationPolicy<T, I> each(
        Function<T, Collection<I>> reader) {
        return new PluralMemberValidationPolicy<>(reader);
    }

    private PluralMemberValidationPolicy(Function<T, Collection<I>> reader) {
        this.reader = reader;
    }
    
    public PluralMemberValidationPolicy<T, I> with(
        Predicate<I> validator) {
            this.validator = validator;
            return this;
    }

    public PluralMemberValidationPolicy<T, I> ofViolation(BiConsumer<I, ValidationsContext<T>> violationWriter) {
    	this.violationWriter = violationWriter;
    	return this;
    }
    
   @Override
    public boolean validate(T model, ValidationsContext<T> ctx) {
        Optional<Collection<I>> collection = Optional.ofNullable(model)
        		.map(reader);
        
		return collection.map(clctn -> clctn.stream().allMatch(
            item -> validator.test(item)))
				.filter(x -> Boolean.TRUE.equals(x))
				.orElseGet(() -> {
					collection.ifPresent(clctn -> {
						clctn.stream().forEach(c -> violationWriter.accept(c, ctx));
					});
					
					return false;
				});
    }

}