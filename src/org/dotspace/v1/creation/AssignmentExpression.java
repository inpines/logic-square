package org.dotspace.v1.creation;

import java.util.function.Predicate;

public interface AssignmentExpression<T, V> {

	public AssignmentExpression<T, V> filter(Predicate<T> predicate);
	
	public void assign(T instance);
	
}
