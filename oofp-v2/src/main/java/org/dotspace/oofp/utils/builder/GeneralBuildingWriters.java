package org.dotspace.oofp.utils.builder;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

@UtilityClass
public class GeneralBuildingWriters {

	public <T, V> GeneralBuildingWriter<T> set(BiConsumer<T, V> setter, V value) {
		return GeneralBuildingWriter.of(setter, value);
	}
	
	public <T, M, V> GeneralBuildingWriter<T> set(Function<T, M> getter,
			BiConsumer<M, V> setter, V value) {
		return GeneralBuildingWriter.of(getter, setter, value);
	}

	public <T, I, D> GeneralBuildingWriter<T> setForEach(
			Function<T, Collection<I>> getter, Function<D, I> itemSelector, 
			Collection<D> collection) {
		return GeneralBuildingWriter.setForEach(getter, itemSelector, collection);
	}

	public <T, D> GeneralBuildingWriter<T> setForEach(
			BiConsumer<T, D> setter, Collection<D> collection) {
		return GeneralBuildingWriter.setForEach(setter, collection);
	}

}
