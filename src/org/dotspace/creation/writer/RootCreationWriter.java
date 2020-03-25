package org.dotspace.creation.writer;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.dotspace.creation.CreationWriter;

public class RootCreationWriter<T, V, C> implements CreationWriter<T, V> {

	private CreationWriterCondition<C> condition;
	
	private BiConsumer<T, V> setter;

	private V value;
	
	public static <T, V, C> RootCreationWriter<T, V, C> conditional(
			BiConsumer<T, V> setter, V value, 
			Predicate<C> predicate, C condition) {
		return new RootCreationWriter<T, V, C>(setter, value, predicate, condition);
	}
	
	public static <T, V> RootCreationWriter<T, V, V> valueConditional(
			BiConsumer<T, V> setter, V value, Predicate<V> predicate) {
		return new RootCreationWriter<>(setter, value, predicate, value);
	}
	
	public static <T, V> RootCreationWriter<T, V, V> noneConditional(
			BiConsumer<T, V> setter, V value) {
		return new RootCreationWriter<>(setter, value, null, null);
	}
	
	protected RootCreationWriter(BiConsumer<T, V> setter, V value, 
			Predicate<C> predicate, C cond) {
		super();
		this.setter = setter;
		this.condition = new CreationWriterCondition<>(predicate, cond);
		this.value = value;
	}

	@Override
	public void write(T instance) {
		
		if (!condition.isPresent() || null == setter) {
			return;
		}
		
		setter.accept(instance, value);
	}

}
