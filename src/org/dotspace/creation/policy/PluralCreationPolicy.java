package org.dotspace.creation.policy;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class PluralCreationPolicy<T, I, V, C> extends CreationPolicyBase<T, V, C> {

	private Function<T, Collection<I>> getter;
	private Function<V, I> itemSelector;
	private Collection<V> values;
	
	public static <T, I, V, C> PluralCreationPolicy<T, I, V, C> withAssignment(
			Function<T, Collection<I>> getter, Function<V, I> itemSelector, 
			Collection<V> values) {
		return new PluralCreationPolicy<>(getter, itemSelector, values);
	}
	
	protected PluralCreationPolicy(Function<T, Collection<I>> getter, 
			Function<V, I> itemSelector,
			Collection<V> values) {
		super();
		this.getter = getter;
		this.itemSelector = itemSelector;
		this.values = values;
	}

	@Override
	public void write(T instance) {
		if (!isConditionPresent(instance) || null == itemSelector) {
			return;
		}
	
		Collection<I> items =Optional.ofNullable(instance)
				.map(getter)
				.orElse(null);
		
		if (null == items) {
			return;
		}
		
		values.stream().map(itemSelector)
		.forEach(item -> items.add(item));
		
	}

}
