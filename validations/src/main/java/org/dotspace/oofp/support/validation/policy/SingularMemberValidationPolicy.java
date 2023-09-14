package org.dotspace.oofp.support.validation.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.oofp.support.validation.ValidationContext;

public class SingularMemberValidationPolicy<T, I> extends BaseValidationPolicy<T> {
    
    private Function<T, I> reader;
    private List<Predicate<I>> validators = new ArrayList<>();
    private BiConsumer<I, ValidationContext<T>> violationWriter;

    public static <T, I> SingularMemberValidationPolicy<T, I> select(Function<T, I> reader) {
        return new SingularMemberValidationPolicy<>(reader);
    }
    
    protected SingularMemberValidationPolicy(Function<T, I> reader) {
        this.reader = reader;
    }

    public SingularMemberValidationPolicy<T, I> filter(Predicate<I> validator) {
            this.validators.add(validator);
            return this;
    }

    public SingularMemberValidationPolicy<T, I> orElse(BiConsumer<I, ValidationContext<T>> violationWriter) {
    	this.violationWriter = violationWriter;
    	return this;
    }
    
    @Override
    public boolean validate(T model, ValidationContext<T> ctx) {
        Optional<I> data = Optional.ofNullable(model)
        		.map(reader);
        
		return data
				.map(mdl -> validators.stream()
						.filter(Objects::nonNull)
						.allMatch(vldtr -> vldtr.test(mdl)))
				.filter(x -> Boolean.TRUE.equals(x))
				.orElseGet(() -> {
					violationWriter.accept(data.orElse(null), ctx);
					return false;
				});
    }

}