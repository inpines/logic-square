package org.dotspace.oofp.support.builder.writer;

import java.util.function.BiConsumer;

public class RootWriter<T, V> extends GeneralBuildingWriterBase<T, V> {

	private BiConsumer<T, V> setter;

	private V value;
	
	public static <T, V, C> RootWriter<T, V> get(
			BiConsumer<T, V> setter, V value) {
		return new RootWriter<>(setter, value);
	}
	
	private RootWriter(BiConsumer<T, V> setter, V value) {
		super();
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
		
		setter.accept(instance, value);
	}

}
