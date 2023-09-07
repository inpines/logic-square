package org.dotspace.oofp.support.builder.writer;

import java.util.Collection;
import java.util.function.BiConsumer;

public class RootRepeatWriter<T, I, D> extends GeneralBuildingWriterBase<T, Collection<D>> {

	private BiConsumer<T, D> setter;
	private Collection<D> datas;
	
	public static <T, D> GeneralBuildingWriterBase<T, Collection<D>> get(
			BiConsumer<T, D> setter, Collection<D> datas) {
		return new RootRepeatWriter<>(setter, datas);
	}
	private RootRepeatWriter(
			BiConsumer<T, D> setter, Collection<D> datas) {
		this.setter = setter;
		this.datas = datas;
	}
	
	@Override
	public void write(T instance) {
		datas.forEach(d -> setter.accept(instance, d));
	}

}
