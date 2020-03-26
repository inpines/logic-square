package org.dotspace.creation;

public interface AssignmentExpression<T, V> {

	public <C> AssignmentExpression<T, V> filter(PredicateExpression<T, C> condition);
	
	public void assign(T instance);
	
}
