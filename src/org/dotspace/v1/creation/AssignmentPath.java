package org.dotspace.v1.creation;

public interface AssignmentPath<T, D> {

	public AssignmentExpression<T, D> assign(D data);
	
}
