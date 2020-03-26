package org.dotspace.creation;

public interface AssignmentExpression<T, V> {

	public <C> AssignmentExpression<T, V> filter(AssignmentPredicate<T, C> condition);
	
	public void assign(T instance);
	
}
