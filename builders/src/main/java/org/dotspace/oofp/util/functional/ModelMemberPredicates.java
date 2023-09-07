package org.dotspace.oofp.util.functional;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

@Component
public class ModelMemberPredicates {

	public <T, V> Predicate<T> validatePresent(Function<T, V> reader) {
		return model -> Optional.ofNullable(model)
				.map(reader)
				.filter(Predicates.not(Objects::isNull))
				.isPresent();
	}
	
}
