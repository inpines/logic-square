package org.dotspace.oofp.support.builder.writer;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SingularMemberWriter<T, M, V> extends GeneralBuildingWriterBase<T, V> {

	private Function<T, M> getter;
	private BiConsumer<M, V> setter;
	private V value;
		
	private SingularMemberWriter(Function<T, M> getter, 
			BiConsumer<M, V> setter, V value) {
		this.getter = getter;
		this.setter = setter;
		this.value = value;
	}
	
	@Override
	public void write(T instance) {
		
		if (!isConditionPresent(instance) || null == setter) {
			return;
		}
		
		if (!isConditionPresentByValue(value)) {
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

	public static <T, M, V> SingularMemberWriter<T, M, V> get(
			Function<T, M> getter, BiConsumer<M, V> setter, V value) {
		return new SingularMemberWriter<>(getter, setter, value);
	}

}
