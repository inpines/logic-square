package org.dotspace.oofp.utils.builder;

import lombok.Builder;
import lombok.NonNull;
import org.dotspace.oofp.utils.functional.Predicates;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface GeneralBuildingWriter<T> {

	default <C> GeneralBuildingWriter<T> filter(Predicate<C> predicate, C condition) {
		return instance -> {
			if (predicate.test(condition)) {
				write(instance);
			}
		};
	}

	@Builder(setterPrefix = "with")
	class SimpleValueWriter<T, V> implements GeneralBuildingWriter<T> {

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
	class ApplyValueWriter<T, V> implements GeneralBuildingWriter<T> {

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

	default GeneralBuildingWriter<T> filter(@NonNull Predicate<T> predicate) {
		return t -> Optional.ofNullable(t)
				.filter(predicate)
				.ifPresent(this::write);
	}

	default <C> GeneralBuildingWriter<T> filterWithCondition(
            Predicate<C> predicate, @NonNull C condition) {
		return t -> {
			if (Optional.of(condition)
					.filter(predicate)
					.isPresent()) {
				write(t);
			}
		};
	}

	default GeneralBuildingWriter<T> when(@NonNull Predicate<T> condition) {
		return instance -> Optional.of(instance)
				.filter(condition)
				.ifPresent(this::write);
	}

	default GeneralBuildingWriter<T> peekBefore(@NonNull Consumer<T> observer) {
		return instance -> {
			observer.accept(instance);
			write(instance);
		};
	}

	default GeneralBuildingWriter<T> peekAfter(@NonNull Consumer<T> observer) {
		return instance -> {
			write(instance);
			observer.accept(instance);
		};

	}
	static <T, V> GeneralBuildingWriter<T> of(@NonNull BiConsumer<T, V> setter, V value) {
		return SimpleValueWriter.<T, V>builder()
				.withSetter(setter)
				.withValue(value)
				.build();
	}

	static <T, U, V> GeneralBuildingWriter<T> of(
			@NonNull Function<T, U> getter, @NonNull BiConsumer<U, V> setter, V value) {
		return instance -> Optional.of(instance)
				.map(getter)
				.ifPresent(GeneralBuildingWriter.of(setter, value)::write);
	}

	static <T, V> GeneralBuildingWriter<T> fromFunction(
			@NonNull BiConsumer<T, V> setter, @NonNull Function<T, V> getter) {
		return ApplyValueWriter.<T, V>builder()
				.withApplier(getter)
				.withSetter(setter)
				.build();
	}

	static <T, V> GeneralBuildingWriter<T> when(
			@NonNull Predicate<V> predicate, @NonNull BiConsumer<T, V> setter, V value) {
		return SimpleValueWriter.<T, V>builder()
				.withPredicate(predicate)
				.withSetter(setter)
				.withValue(value)
				.build();
	}

	static <T, V> GeneralBuildingWriter<T> when(
			@NonNull Predicate<V> predicate, @NonNull Function<T, V> getter, @NonNull BiConsumer<T, V> setter) {
		return ApplyValueWriter.<T, V>builder()
				.withPredicate(predicate)
				.withApplier(getter)
				.withSetter(setter)
				.build();
	}

	static <T, I, D> GeneralBuildingWriter<T> setForEach(
			@NonNull Function<T, Collection<I>> getter, @NonNull Function<D, I> itemSelector,
			@NonNull Collection<D> collection) {
		return PluralMemberWriter.<T, I, D>builder()
				.withGetter(getter)
				.withItemSelector(itemSelector)
				.withCollection(collection)
				.build();
	}

	static <T, I, D> GeneralBuildingWriter<T> setForEach(
			@NonNull Function<T, Collection<I>> getter, @NonNull Function<D, I> itemSelector,
			@NonNull Predicate<I> predicate, @NonNull Collection<D> collection) {
		return PluralMemberWriter.<T, I, D>builder()
				.withGetter(getter)
				.withItemSelector(itemSelector)
				.withPredicate(predicate)
				.withCollection(collection)
				.build();
	}

	static <T, D> GeneralBuildingWriter<T> setForEach(
			@NonNull BiConsumer<T, D> setter, @NonNull Collection<D> collection) {
		return instance -> collection.forEach(
				item -> setter.accept(instance, item)
		);
	}

	void write(T instance);
	
}
