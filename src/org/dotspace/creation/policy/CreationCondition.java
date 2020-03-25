package org.dotspace.creation.policy;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.creation.functional.Predicates;

public class CreationCondition<T, C> {

	protected Predicate<C> predicate;
	
	private Function<T, C> condReader;
	
	public CreationCondition(Predicate<C> predicate, Function<T, C> condReader) {
		this.predicate = predicate;
		this.condReader = condReader;
	}

	public boolean isPresent(T instance) {
		if (null == predicate) {
			return true;
		}
		
		C cond = Optional.ofNullable(instance)
				.map(condReader)
				.orElse(null);
		
		return predicate.test(cond);
		
	}

	public static <T, V> CreationCondition<T, V> forValuePresent(V value) {
		return new CreationCondition<>(Predicates.forPresent(), inst -> value);
	}

	public static <T, C> CreationCondition<T, C> forPredicate(
			Predicate<C> predicate, C cond) {
		return new CreationCondition<>(predicate, inst -> cond);
	}
}
