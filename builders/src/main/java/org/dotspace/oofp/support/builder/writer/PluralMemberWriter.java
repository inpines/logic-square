package org.dotspace.oofp.support.builder.writer;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class PluralMemberWriter<T, I, V> extends GeneralBuildingWriterBase<T, Collection<V>> {

	private Function<T, Collection<I>> getter;
	private Function<V, I> itemSelector;
	private Collection<V> values;
	
	public static <T, I, V> PluralMemberWriter<T, I, V> get(
			Function<T, Collection<I>> getter, Function<V, I> itemSelector, 
			Collection<V> values) {
		return new PluralMemberWriter<>(getter, itemSelector, values);
	}
	
	protected PluralMemberWriter(Function<T, Collection<I>> getter, 
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
	
		Collection<I> items = Optional.ofNullable(instance)
				.map(getter)
				.orElse(null);
		
		if (null == items) {
			return;
		}
		
		Optional.ofNullable(values)
		.filter(this::isConditionPresentByValue)
		.ifPresent(v -> v.stream()
				.map(itemSelector)
				.forEach(item -> items.add(item)));
		
	}

}
