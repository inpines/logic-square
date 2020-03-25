package org.dotspace.creation;

import org.dotspace.creation.policy.CreationCondition;

public interface CreationPolicy<T, V, C> {

	public CreationPolicy<T, V, C> when(CreationCondition<T, C> condition);
	
	public void write(T instance);
	
}
