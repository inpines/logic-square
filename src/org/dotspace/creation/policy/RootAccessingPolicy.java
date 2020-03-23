package org.dotspace.creation.policy;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.dotspace.creation.AccessingPolicy;

public class RootAccessingPolicy<T, V> implements AccessingPolicy<T, V> {

	private BiConsumer<T, V> accessor;

	private V value;
	
	public RootAccessingPolicy(BiConsumer<T, V> accessor, V value) {
		this.accessor = accessor;
		this.value = value;
	}

	@Override
	public void access(T instance) {
		Optional.ofNullable(value).ifPresent(v -> accessor.accept(instance, v));
	}

}
