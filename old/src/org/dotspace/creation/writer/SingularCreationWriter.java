package org.dotspace.creation.writer;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.dotspace.creation.CreationWriter;

public class SingularCreationWriter<T, M, V, C> implements CreationWriter<T, V> {

	private Function<T, M> getter;
	private BiConsumer<M, V> setter;
	private V value;
	
	private CreationWriterCondition<C> condition;
	
	public static <T, M, V, C> CreationWriter<T, V> noneConditional(Function<T, M> getter, 
			BiConsumer<M, V> setter, V value) {
		return new SingularCreationWriter<>(getter, setter, value, null, null);
	}

	public static <T, M, V, C> CreationWriter<T, V> conditional(Function<T, M> getter, 
			BiConsumer<M, V> setter, V value, Predicate<C> predicate, C cond) {
		return new SingularCreationWriter<>(getter, setter, value, predicate, cond);
	}

	protected SingularCreationWriter(Function<T, M> getter, BiConsumer<M, V> setter, V value,
			Predicate<C> predicate, C condition) {
		this.getter = getter;
		this.setter = setter;
		this.value = value;
		this.condition = new CreationWriterCondition<>(predicate, condition);
	}

	@Override
	public void write(T instance) {
		if (!condition.isPresent() || null == setter) {
			return;
		}
		
		M member = Optional.ofNullable(getter)
				.map(reader -> reader.apply(instance))
				.orElse(Optional.ofNullable(instance)
						.map(inst -> cast(inst))
						.orElse(null));
		
		setter.accept(member, value);
	}

	private M cast(T inst) {
		@SuppressWarnings("unchecked")
		M result = (M) inst;
		return result;
	}

}
