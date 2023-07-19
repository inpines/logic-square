package org.dotspace.oofp.support.builder.writer;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.dotspace.oofp.support.builder.GeneralBuildingWriter;

public class GeneralBuildingWriters {

	public static <T, V> GeneralBuildingWriter<T, V> set(BiConsumer<T, V> setter, V value) {
		return RootWriter.get(setter, value);
	}
	
	public static <T, V> GeneralBuildingWriter<T, V> setByFunction(
			BiConsumer<T, V> setter, Function<T, V> f) {
		return RootValueFunctorWriter.get(setter, f);
	}
	
	public static <T, M, V> GeneralBuildingWriter<T, V> set(Function<T, M> getter,
			BiConsumer<M, V> setter, V value) {
		return SingularMemberWriter.get(getter, setter, value);
	}
	
	public static <T, I, D> GeneralBuildingWriter<T, Collection<D>> setForEach(
			Function<T, Collection<I>> getter, Function<D, I> itemSelector, 
			Collection<D> datas) {
		return PluralMemberWriter.get(getter, itemSelector, datas);
	}

	public static <T, D> GeneralBuildingWriter<T, Collection<D>> setForEach(
			BiConsumer<T, D> setter, Collection<D> datas) {
		return RootRepeatWriter.get(setter, datas);
	}
	
}
