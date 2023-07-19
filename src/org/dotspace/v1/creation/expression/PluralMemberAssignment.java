package org.dotspace.v1.creation.expression;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class PluralMemberAssignment<T, I, V> extends AssignmentExpressionBase<T, Collection<V>> {

	private Function<T, Collection<I>> getter;
	private Function<V, I> itemSelector;
	private Collection<V> values;
	
	public static <T, I, V> PluralMemberAssignment<T, I, V> get(
			Function<T, Collection<I>> getter, Function<V, I> itemSelector, 
			Collection<V> values) {
		return new PluralMemberAssignment<>(getter, itemSelector, values);
	}
	
	protected PluralMemberAssignment(Function<T, Collection<I>> getter, 
			Function<V, I> itemSelector,
			Collection<V> values) {
		super();
		this.getter = getter;
		this.itemSelector = itemSelector;
		this.values = values;
	}

	@Override
	public void assign(T instance) {
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
