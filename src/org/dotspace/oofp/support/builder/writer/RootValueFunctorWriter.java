package org.dotspace.oofp.support.builder.writer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class RootValueFunctorWriter<T, V> extends GeneralBuildingWriterBase<T, V> {

	private BiConsumer<T, V> setter;

	private Function<T, V> valueFunctor;
	
	public static <T, V, C> RootValueFunctorWriter<T, V> get(
			BiConsumer<T, V> setter, Function<T, V> valueFunctor) {
		return new RootValueFunctorWriter<>(setter, valueFunctor);
	}
	
	private RootValueFunctorWriter(BiConsumer<T, V> setter, Function<T, V> valueFunctor) {
		super();
		this.setter = setter;
		this.valueFunctor = valueFunctor;
	}

	@Override
	public void write(T instance) {
		
		if (!isConditionPresent(instance) || null == setter) {
			return;
		}
		
		V valueToSet = valueFunctor.apply(instance);
		
		if (!isConditionPresentByValue(valueToSet)) {
			return;
		}
		
		setter.accept(instance, valueToSet);
	}

}
