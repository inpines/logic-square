package org.dotspace.oofp.utils.builder;

import org.dotspace.oofp.utils.functional.Predicates;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class WritingPredicates<T, C> implements Predicate<T> {

	private final Predicate<C> predicate;
	
	private final Function<T, C> stateSelector;
	
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

	public static <T, V> WritingPredicates<T, V> ifApplierMatch(
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
