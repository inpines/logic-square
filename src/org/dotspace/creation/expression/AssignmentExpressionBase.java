package org.dotspace.creation.expression;

import org.dotspace.creation.AssignmentExpression;
import org.dotspace.creation.AssignmentPredicate;

public abstract class AssignmentExpressionBase<T, V> implements AssignmentExpression<T, V> {

	protected AssignmentPredicate<T, ?> condition;
	
	@Override
	public <C> AssignmentExpression<T, V> filter(AssignmentPredicate<T, C> condition) {
		this.condition = condition;
		return this;
	}

	protected boolean isConditionPresent(T instance) {
		if (null == condition) {
			return true;
		}
		
		return condition.isPresent(instance);
	}
	
}
