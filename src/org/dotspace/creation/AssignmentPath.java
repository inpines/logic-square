package org.dotspace.creation;

public interface AssignmentPath<T, D> {

	public AssignmentExpression<T, D> assign(D data);
	
}
