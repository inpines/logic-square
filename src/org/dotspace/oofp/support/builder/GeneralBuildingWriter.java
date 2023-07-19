package org.dotspace.oofp.support.builder;

import java.util.function.Predicate;

public interface GeneralBuildingWriter<T, V> {

	public GeneralBuildingWriter<T, V> filterByValue(Predicate<V> predicate);
	
	public GeneralBuildingWriter<T, V> filter(Predicate<T> predicate);
	
	public <C> GeneralBuildingWriter<T, V> filterWithCondition(
			Predicate<C> predicate, C condition);
	
	public void write(T instance);
	
}
