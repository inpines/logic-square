package org.dotspace.creation.policy;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SingularCreationPolicy<T, M, V, C> extends CreationPolicyBase<T, V, C> {

	private Function<T, M> getter;
	private BiConsumer<M, V> setter;
	private V value;
		
//	public static <T, M, V, C> CreationPolicy<T, V, C> noneConditional(Function<T, M> getter, 
//			BiConsumer<M, V> setter, V value) {
//		return new SingularCreationPolicy<>(getter, setter, value, null, null);
//	}
//
//	public static <T, M, V, C> CreationPolicy<T, V, C> conditional(Function<T, M> getter, 
//			BiConsumer<M, V> setter, V value, Predicate<C> predicate, C cond) {
//		return new SingularCreationPolicy<>(getter, setter, value, predicate, cond);
//	}

//	protected SingularCreationPolicy(Function<T, M> getter, BiConsumer<M, V> setter, V value,
//			Predicate<C> predicate, C condition) {
//		this.getter = getter;
//		this.setter = setter;
//		this.value = value;
//		this.condition = new CreationCondition<>(predicate, condition);
//	}

	private SingularCreationPolicy(Function<T, M> getter, 
			BiConsumer<M, V> setter, V value) {
		this.getter = getter;
		this.setter = setter;
		this.value = value;
	}
	
	@Override
	public void write(T instance) {
		if (!isConditionPresent(instance) || null == setter) {
			return;
		}
		
		M member = Optional.ofNullable(getter)
				.map(reader -> reader.apply(instance))
				.orElse(Optional.ofNullable(instance)
						.map(inst -> cast(inst))
						.orElse(null));
		
		setter.accept(member, value);
	}

	private M cast(T inst) {
		@SuppressWarnings("unchecked")
		M result = (M) inst;
		return result;
	}

	public static <T, M, V, C> SingularCreationPolicy<T, M, V, C> withAssignment(
			Function<T, M> getter, BiConsumer<M, V> setter, V value) {
		return new SingularCreationPolicy<>(getter, setter, value);
	}

}
