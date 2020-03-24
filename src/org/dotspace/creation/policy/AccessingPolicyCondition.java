package org.dotspace.creation.policy;

import java.util.function.Predicate;

public class AccessingPolicyCondition<C> {

	private Predicate<C> predicate;
	
	private C cond;
	
	public AccessingPolicyCondition(Predicate<C> predicate, C cond) {
		this.predicate = predicate;
		this.cond = cond;
	}

	public boolean isValid() {
		if (null == predicate) {
			return true;
		}
		
		return predicate.test(cond);
	}
}
