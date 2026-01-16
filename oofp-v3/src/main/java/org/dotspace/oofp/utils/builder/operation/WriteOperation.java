package org.dotspace.oofp.utils.builder.operation;

import org.dotspace.oofp.utils.functional.Predicates;

import lombok.Builder;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface WriteOperation<T> {

	default <C> WriteOperation<T> require(Predicate<C> predicate, C condition) {
		return t -> {
			if (predicate.test(condition)) {
				write(t);
			}
		};
	}

	@Builder(setterPrefix = "with")
	class SimpleValueWriter<T, V> implements WriteOperation<T> {

		private V value;

		@NonNull
		@Builder.Default
		private Predicate<V> predicate = Predicates.ok();
		private BiConsumer<T, V> setter;

		@Override
		public void write(T instance) {
			Optional.ofNullable(instance).ifPresent(t -> {
				if (predicate.test(value)) { // 不用 monad 因為考慮 value 的情況
					setter.accept(t, value);
				}
			});
		}
	}

	@Builder(setterPrefix = "with")
	class ApplyValueWriter<T, V> implements WriteOperation<T> {

		@NonNull
		private Function<T, V> applier;

		@NonNull
		@Builder.Default
		private Predicate<V> predicate = Predicates.ok();
		private BiConsumer<T, V> setter;

		@Override
		public void write(T instance) {
			Optional.ofNullable(instance).ifPresent(t -> {
				V value = applier.apply(t);

				if (predicate.test(value)) { // 要考慮 value 為 null 的情況
					setter.accept(t, value);
				}
			});
		}
	}

	default WriteOperation<T> require(@NonNull Predicate<T> predicate) {
		return t -> Optional.of(t)
				.filter(predicate)
				.ifPresent(this::write);
	}

	default WriteOperation<T> peekBefore(@NonNull Consumer<T> observer) {
		return t -> {
			observer.accept(t);
			write(t);
		};
	}

	default WriteOperation<T> peekAfter(@NonNull Consumer<T> observer) {
		return t -> {
			write(t);
			observer.accept(t);
		};

	}

	static <T> WriteOperation<T> wrap(@NonNull Consumer<T> consumer) {
		return consumer::accept;
	}

	static <T, V> WriteOperation<T> from(@NonNull BiConsumer<T, V> setter, @NonNull Function<T, V> valueProvider) {
		return t -> setter.accept(t, valueProvider.apply(t));
	}

	static <T, V> WriteOperation<T> set(@NonNull BiConsumer<T, V> setter, V value) {
		return SimpleValueWriter.<T, V>builder()
				.withSetter(setter)
				.withValue(value)
				.build();
	}

	static <T, U, V> WriteOperation<T> setOn(
			@NonNull Function<T, U> getter, @NonNull BiConsumer<U, V> setter, V value) {
		return instance -> Optional.of(instance)
				.map(getter)
				.ifPresent(WriteOperation.set(setter, value)::write);
	}

	static <T, V> WriteOperation<T> when(
			@NonNull Predicate<V> predicate, @NonNull BiConsumer<T, V> setter, V value) {
		return SimpleValueWriter.<T, V>builder()
				.withPredicate(predicate)
				.withSetter(setter)
				.withValue(value)
				.build();
	}

	static <T, V> WriteOperation<T> when(
			@NonNull Predicate<V> predicate, @NonNull Function<T, V> getter, @NonNull BiConsumer<T, V> setter) {
		return ApplyValueWriter.<T, V>builder()
				.withPredicate(predicate)
				.withApplier(getter)
				.withSetter(setter)
				.build();
	}

	static <T, I, D> WriteOperation<T> setForEach(
			@NonNull Function<T, Collection<I>> getter, @NonNull Function<D, I> itemSelector,
			@NonNull Collection<D> collection) {
		return t -> writePluralMember(t, getter, itemSelector, Predicates.ok(), collection);
	}

	static <T, I, D> void writePluralMember(
			T instance, @NonNull Function<T, Collection<I>> getter, @NonNull Function<D, I> itemSelector,
			@NonNull Predicate<I> predicate, @NonNull Collection<D> collection) {
		var items = getter.apply(instance);

		collection.forEach(v ->
				Optional.ofNullable(itemSelector.apply(v))
						.filter(predicate)
						.ifPresent(items::add)
		);
	}

	static <T, I, D> WriteOperation<T> setForEach(
			@NonNull Function<T, Collection<I>> getter, @NonNull Function<D, I> itemSelector,
			@NonNull Predicate<I> predicate, @NonNull Collection<D> collection) {
		return t -> writePluralMember(t, getter, itemSelector, predicate, collection);
	}

	static <T, D> WriteOperation<T> setForEach(
			@NonNull BiConsumer<T, D> setter, @NonNull Collection<D> collection) {
		return t -> collection.forEach(
				item -> setter.accept(t, item)
		);
	}

	void write(T t);
	
}
