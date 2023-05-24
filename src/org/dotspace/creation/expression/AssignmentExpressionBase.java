package org.dotspace.creation.expression;

import java.util.Optional;
import java.util.function.Predicate;

import org.dotspace.creation.AssignmentExpression;

public abstract class AssignmentExpressionBase<T, V> implements AssignmentExpression<T, V> {

	protected Predicate<T> predicate;
	
	@Override
	public AssignmentExpression<T, V> filter(Predicate<T> predicate) {
		this.predicate = predicate;
		return this;
	}

	protected boolean isConditionPresent(T instance) {
		if (null == predicate) {
			return true;
		}
		
		return Optional.ofNullable(instance).filter(predicate).isPresent();
	}
	
}
