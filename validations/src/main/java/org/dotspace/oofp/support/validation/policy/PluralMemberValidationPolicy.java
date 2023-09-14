package org.dotspace.oofp.support.validation.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.oofp.support.validation.ValidationContext;

public class PluralMemberValidationPolicy<T, I> extends BaseValidationPolicy<T> {

    private Function<T, Collection<I>> reader;
    
    private List<Predicate<I>> validators = new ArrayList<>();

    private BiConsumer<I, ValidationContext<T>> violationWriter;
    
    public static <T, I> PluralMemberValidationPolicy<T, I> each(
        Function<T, Collection<I>> reader) {
        return new PluralMemberValidationPolicy<>(reader);
    }

    private PluralMemberValidationPolicy(Function<T, Collection<I>> reader) {
        this.reader = reader;
    }
    
    public PluralMemberValidationPolicy<T, I> filter(
        Predicate<I> validator) {
            this.validators.add(validator);
            return this;
    }

    public PluralMemberValidationPolicy<T, I> orElse(BiConsumer<I, ValidationContext<T>> violationWriter) {
    	this.violationWriter = violationWriter;
    	return this;
    }
    
   @Override
    public boolean validate(T model, ValidationContext<T> ctx) {
        Optional<Collection<I>> collection = Optional.ofNullable(model)
        		.map(reader);
        
		return collection.map(clctn -> clctn.stream()
				.allMatch(item -> validators.stream()
						.filter(Objects::nonNull)
						.allMatch(vldtr -> vldtr.test(item))))
				.filter(x -> Boolean.TRUE.equals(x))
				.orElseGet(() -> {
					collection.ifPresent(clctn -> {
						clctn.stream().forEach(c -> violationWriter.accept(c, ctx));
					});
					
					return false;
				});
    }

}