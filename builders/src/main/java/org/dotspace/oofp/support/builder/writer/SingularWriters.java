package org.dotspace.oofp.support.builder.writer;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.dotspace.oofp.support.builder.GeneralBuildingWriter;
import org.dotspace.oofp.support.builder.GeneralBuildingWriterOperator;

public class SingularWriters<T, M, V> implements GeneralBuildingWriterOperator<T, V> {

	private Function<T, M> getter;
	private BiConsumer<M, V> setter;
	
	public static <T, M, V> SingularWriters<T, M, V> getPathToSet(Function<T, M> getter,
			BiConsumer<M, V> setter) {
		return new SingularWriters<>(getter, setter);
	}
	
	public static <T, V> SingularWriters<T, T, V> getRootToSet(BiConsumer<T, V> setter) {
		return new SingularWriters<>(t -> t, setter);
	}
	
	private SingularWriters(Function<T, M> getter, BiConsumer<M, V> setter) {
		super();
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public GeneralBuildingWriter<T, V> write(V value) {
		return SingularMemberWriter.get(getter, setter, value);
	}

}
