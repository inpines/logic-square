package org.dotspace.creation.policy;

import java.util.function.BiConsumer;

public class RootCreationPolicy<T, V, C> extends CreationPolicyBase<T, V, C> {

	private BiConsumer<T, V> setter;

	private V value;
	
//	public static <T, V, C> RootCreationPolicy<T, V, C> conditional(
//			BiConsumer<T, V> setter, V value, 
//			Predicate<C> predicate, C condition) {
//		return new RootCreationPolicy<T, V, C>(setter, value, predicate, condition);
//	}
//	
//	public static <T, V> RootCreationPolicy<T, V, V> valueConditional(
//			BiConsumer<T, V> setter, V value, Predicate<V> predicate) {
//		return new RootCreationPolicy<>(setter, value, predicate, value);
//	}
//	
//	public static <T, V> RootCreationPolicy<T, V, V> noneConditional(
//			BiConsumer<T, V> setter, V value) {
//		return new RootCreationPolicy<>(setter, value, null, null);
//	}
//	
//	protected RootCreationPolicy(BiConsumer<T, V> setter, V value, 
//			Predicate<C> predicate, C cond) {
//		super();
//		this.setter = setter;
//		this.condition = new CreationCondition<>(predicate, cond);
//		this.value = value;
//	}
	
	public static <T, V, C> RootCreationPolicy<T, V, C> withAssignment(
			BiConsumer<T, V> setter, V value) {
		return new RootCreationPolicy<>(setter, value);
	}
	
	private RootCreationPolicy(BiConsumer<T, V> setter, V value) {
		super();
		this.setter = setter;
		this.value = value;
	}

	@Override
	public void write(T instance) {
		
		if (!isConditionPresent(instance) || null == setter) {
			return;
		}
		
		setter.accept(instance, value);
	}

}
