package org.dotspace.creation;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.creation.functional.Predicates;

public class PredicateExpression<T, C> {

	private Predicate<C> predicate;
	
	private Function<T, C> stateSelector;
	
	protected PredicateExpression(Predicate<C> predicate, 
			Function<T, C> stateSelector) {
		super();
		this.predicate = predicate;
		this.stateSelector = stateSelector;
	}

	public boolean isPresent(T instance) {
		if (null == predicate) {
			return true;
		}
		
		C cond = Optional.ofNullable(instance)
				.map(stateSelector)
				.orElse(null);
		
		return predicate.test(cond);
		
	}

	public static <T, V> PredicateExpression<T, V> ifPresent(V value) {
		return new PredicateExpression<>(Predicates.forPresent(), inst -> value);
	}

	public static <T, C> PredicateExpression<T, C> ifMatch(
			Predicate<C> predicate, C condition) {
		return new PredicateExpression<>(predicate, inst -> condition);
	}
	
	public static <T, V> PredicateExpression<T, V> ifMatch(
			Predicate<V> predicate, Function<T, V> valueSelector) {
		return new PredicateExpression<>(predicate, valueSelector);
	}
	
}
