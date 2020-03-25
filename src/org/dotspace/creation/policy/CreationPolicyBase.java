package org.dotspace.creation.policy;

import org.dotspace.creation.CreationPolicy;

public abstract class CreationPolicyBase<T, V, C> implements CreationPolicy<T, V, C> {

	protected CreationCondition<T, C> condition;
	
	@Override
	public CreationPolicy<T, V, C> when(CreationCondition<T, C> condition) {
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
