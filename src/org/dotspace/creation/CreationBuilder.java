package org.dotspace.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CreationBuilder<T> {

	private Supplier<T> constructor;
	private List<CreationPolicy<T, ?, ?>> creationPolicies = new ArrayList<>();
	
	public CreationBuilder(Supplier<T> constructor) {
		super();
		this.constructor = constructor;
	}

	public T build() {
		T result = constructor.get();
		creationPolicies.forEach(policy -> policy.write(result));
		return result;
	}

	public <V, C> CreationBuilder<T> take(CreationPolicy<T, V, C> policy) {
		creationPolicies.add(policy);
		return this;
	}
	
//	public <V> CreationBuilder<T> take(BiConsumer<T, V> setter, V value) {
//		creationPolicies.add(RootCreationWriter.noneConditional(setter, value));
//		return this;
//	}
//
//	public <V> CreationBuilder<T> setIfPresent(BiConsumer<T, V> setter, V value) {
//		creationPolicies.add(RootCreationWriter.valueConditional(setter, value, 
//				Predicates.present()));
//		return this;
//	}
//
//	public <V, C> CreationBuilder<T> set(BiConsumer<T, V> setter, V value, 
//			Predicate<C> predicate, C cond) {
//		creationPolicies.add(RootCreationWriter.conditional(setter, value, 
//				predicate, cond));
//		return this;
//	}
//
//	public <V, M> CreationBuilder<T> set(Function<T, M> getter, BiConsumer<M, V> setter, 
//			V value) {
//		creationPolicies.add(SingularCreationWriter.noneConditional(getter, setter, value));
//		return this;
//	}
//
//	public <V, M, C> CreationBuilder<T> set(Function<T, M> getter, BiConsumer<M, V> setter, 
//			V value, Predicate<C> predicate, C cond) {
//		creationPolicies.add(SingularCreationWriter.conditional(getter, setter, value, 
//				predicate, cond));
//		return this;
//	};

}
