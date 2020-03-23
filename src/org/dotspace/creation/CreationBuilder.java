package org.dotspace.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.dotspace.creation.policy.RootAccessingPolicy;

public class CreationBuilder<T> {

	private Supplier<T> constructor;
	private List<AccessingPolicy<T, ?>> accessingPolicies = new ArrayList<>();
	
	public CreationBuilder(Supplier<T> constructor) {
		super();
		this.constructor = constructor;
	}

	public T build() {
		T result = constructor.get();
		accessingPolicies.forEach(policy -> policy.access(result));
		return result;
	}

	public <V> CreationBuilder<T> set(BiConsumer<T, V> setter, V value) {
		accessingPolicies.add(new RootAccessingPolicy<>(setter, value));
		return this;
	};

}
