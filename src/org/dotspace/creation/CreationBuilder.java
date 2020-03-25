package org.dotspace.creation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.dotspace.creation.functional.Predicates;
import org.dotspace.creation.writer.RootCreationWriter;
import org.dotspace.creation.writer.SingularCreationWriter;

public class CreationBuilder<T> {

	private Supplier<T> constructor;
	private List<CreationWriter<T, ?>> accessingPolicies = new ArrayList<>();
	
	public CreationBuilder(Supplier<T> constructor) {
		super();
		this.constructor = constructor;
	}

	public T build() {
		T result = constructor.get();
		accessingPolicies.forEach(policy -> policy.write(result));
		return result;
	}

	public <V> CreationBuilder<T> set(BiConsumer<T, V> setter, V value) {
		accessingPolicies.add(RootCreationWriter.noneConditional(setter, value));
		return this;
	}

	public <V> CreationBuilder<T> setIfPresent(BiConsumer<T, V> setter, V value) {
		accessingPolicies.add(RootCreationWriter.valueConditional(setter, value, 
				Predicates.present()));
		return this;
	}

	public <V, C> CreationBuilder<T> set(BiConsumer<T, V> setter, V value, 
			Predicate<C> predicate, C cond) {
		accessingPolicies.add(RootCreationWriter.conditional(setter, value, 
				predicate, cond));
		return this;
	}

	public <V, M> CreationBuilder<T> set(Function<T, M> getter, BiConsumer<M, V> setter, 
			V value) {
		accessingPolicies.add(SingularCreationWriter.noneConditional(getter, setter, value));
		return this;
	}

	public <V, M, C> CreationBuilder<T> set(Function<T, M> getter, BiConsumer<M, V> setter, 
			V value, Predicate<C> predicate, C cond) {
		accessingPolicies.add(SingularCreationWriter.conditional(getter, setter, value, 
				predicate, cond));
		return this;
	};

}
