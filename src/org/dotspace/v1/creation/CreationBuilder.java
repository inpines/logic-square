package org.dotspace.v1.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CreationBuilder<T> {

	private Supplier<T> constructor;
	private List<AssignmentExpression<T, ?>> creationPolicies = new ArrayList<>();
	
	public CreationBuilder(Supplier<T> constructor) {
		super();
		this.constructor = constructor;
	}

	public T build() {
		T result = constructor.get();
		creationPolicies.forEach(policy -> policy.assign(result));
		return result;
	}

	public <V, C> CreationBuilder<T> take(AssignmentExpression<T, V> policy) {
		creationPolicies.add(policy);
		return this;
	}
	
}
