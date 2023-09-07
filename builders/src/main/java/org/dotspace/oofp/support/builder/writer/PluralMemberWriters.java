package org.dotspace.oofp.support.builder.writer;

import java.util.Collection;
import java.util.function.Function;

import org.dotspace.oofp.support.builder.GeneralBuildingWriter;
import org.dotspace.oofp.support.builder.GeneralBuildingWriters;

public class PluralMemberWriters<T, I, D> implements GeneralBuildingWriters<T, Collection<D>> {

	private Function<T, Collection<I>> getter;
	
	private Function<D, I> itemSelector;

	public static <T, I, D> PluralMemberWriters<T, I, D> getToSet(
			Function<T, Collection<I>> getter, Function<D, I> itemSelector) {
		return new PluralMemberWriters<>(getter, itemSelector);
	}
	
	private PluralMemberWriters(Function<T, Collection<I>> getter, 
			Function<D, I> itemSelector) {
		super();
		this.getter = getter;
		this.itemSelector = itemSelector;
	}

	@Override
	public GeneralBuildingWriter<T, Collection<D>> write(Collection<D> data) {
		return PluralMemberWriter.get(getter, itemSelector, data);
	}

}
