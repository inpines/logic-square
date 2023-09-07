package org.dotspace.oofp.support.builder;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.oofp.util.functional.Predicates;

public class WritingPredicates<T, C> implements Predicate<T> {

	private Predicate<C> predicate;
	
	private Function<T, C> stateSelector;
	
	protected WritingPredicates(Predicate<C> predicate, 
			Function<T, C> stateSelector) {
		super();
		this.predicate = predicate;
		this.stateSelector = stateSelector;
	}

	public static <T, V> WritingPredicates<T, V> ifPresent(V value) {
		return new WritingPredicates<>(Predicates.isPresent(), inst -> value);
	}

	public static <T, C> WritingPredicates<T, C> ifMatch(
			Predicate<C> predicate, C condition) {
		return new WritingPredicates<>(predicate, inst -> condition);
	}
	
	public static <T, V> WritingPredicates<T, V> ifMatch(
			Predicate<V> predicate, Function<T, V> valueSelector) {
		return new WritingPredicates<>(predicate, valueSelector);
	}

	@Override
	public boolean test(T instance) {
		if (null == predicate) {
			return true;
		}
		
		C cond = Optional.ofNullable(instance)
				.map(stateSelector)
				.orElse(null);
		
		return predicate.test(cond);
	}
	
}
