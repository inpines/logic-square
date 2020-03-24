package org.dotspace.creation.policy;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.dotspace.creation.AccessingPolicy;

public class RootAccessingPolicy<T, V, C> implements AccessingPolicy<T, V> {

	private AccessingPolicyCondition<C> condition;
	
	private BiConsumer<T, V> accessor;

	private V value;
	
	public static <T, V, C> RootAccessingPolicy<T, V, C> conditional(
			BiConsumer<T, V> accessor, V value, 
			Predicate<C> predicate, C condition) {
		return new RootAccessingPolicy<T, V, C>(accessor, value, predicate, condition);
	}
	
	public static <T, V> RootAccessingPolicy<T, V, V> valueConditional(
			BiConsumer<T, V> accessor, V value, Predicate<V> predicate) {
		return new RootAccessingPolicy<>(accessor, value, predicate, value);
	}
	
	public static <T, V> RootAccessingPolicy<T, V, V> noneConditional(
			BiConsumer<T, V> accessor, V value) {
		return new RootAccessingPolicy<>(accessor, value, null, null);
	}
	
	protected RootAccessingPolicy(BiConsumer<T, V> accessor, V value, 
			Predicate<C> predicate, C cond) {
		super();
		this.accessor = accessor;
		this.condition = new AccessingPolicyCondition<>(predicate, cond);
//		this.predicate = predicate;
//		this.condition = condition;
		this.value = value;
	}

//	public RootAccessingPolicy(BiConsumer<T, V> accessor, V value) {
//		this.accessor = accessor;
//		this.value = value;
//	}

	@Override
	public void access(T instance) {
		
//		if ((null != predicate && !predicate.test(condition)) || null == accessor) {
//			return;
//		}
		
		if (!condition.isValid() || null == accessor) {
			return;
		}
		
		accessor.accept(instance, value);
	}

}
