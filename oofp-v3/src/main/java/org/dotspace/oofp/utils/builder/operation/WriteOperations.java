package org.dotspace.oofp.utils.builder.operation;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class WriteOperations {

	public <T> WriteOperation<T> noop() {
		return t -> {};
	}

	@SafeVarargs
	public <T> WriteOperation<T> chain(WriteOperation<T>... writers) {
		return chain(Arrays.asList(writers));
	}

	public <T> WriteOperation<T> chain(List<WriteOperation<T>> writers) {
		return instance -> {
			if (writers == null || writers.isEmpty()) return;
			for (WriteOperation<T> w : writers) {
				if (w != null) w.write(instance);
			}
		};
	}

	@SafeVarargs
    public <T> WriteOperation<T> whenAll(
			Predicate<T> condition,
			WriteOperation<T>... writers) {
		return WriteOperations.chain(writers).require(condition);
	}

	public <T, V> WriteOperation<T> set(BiConsumer<T, V> setter, V value) {
		return WriteOperation.set(setter, value);
	}
	
	public <T, M, V> WriteOperation<T> set(Function<T, M> getter,
			BiConsumer<M, V> setter, V value) {
		return WriteOperation.setOn(getter, setter, value);
	}

	public <T, I, D> WriteOperation<T> setForEach(
			Function<T, Collection<I>> getter, Function<D, I> itemSelector, 
			Collection<D> collection) {
		return WriteOperation.setForEach(getter, itemSelector, collection);
	}

	public <T, D> WriteOperation<T> setForEach(
			BiConsumer<T, D> setter, Collection<D> collection) {
		return WriteOperation.setForEach(setter, collection);
	}

}
