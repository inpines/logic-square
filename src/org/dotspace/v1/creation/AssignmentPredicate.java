package org.dotspace.creation;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.creation.functional.Predicates;

public class AssignmentPredicate<T, C> implements Predicate<T> {

	private Predicate<C> predicate;
	
	private Function<T, C> stateSelector;
	
	protected AssignmentPredicate(Predicate<C> predicate, 
			Function<T, C> stateSelector) {
		super();
		this.predicate = predicate;
		this.stateSelector = stateSelector;
	}

	public static <T, V> AssignmentPredicate<T, V> ifPresent(V value) {
		return new AssignmentPredicate<>(Predicates.forPresent(), inst -> value);
	}

	public static <T, C> AssignmentPredicate<T, C> ifMatch(
			Predicate<C> predicate, C condition) {
		return new AssignmentPredicate<>(predicate, inst -> condition);
	}
	
	public static <T, V> AssignmentPredicate<T, V> ifMatch(
			Predicate<V> predicate, Function<T, V> valueSelector) {
		return new AssignmentPredicate<>(predicate, valueSelector);
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
