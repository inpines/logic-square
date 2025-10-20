package org.dotspace.oofp.utils.builder;

import lombok.Builder;
import lombok.NonNull;
import org.dotspace.oofp.utils.functional.Predicates;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Builder(setterPrefix = "with")
public class PluralMemberWriter<T, I, V> implements GeneralBuildingWriter<T> {

	@NonNull
	private final Function<T, Collection<I>> getter;

	@Builder.Default
	@NonNull
	private final Predicate<I> predicate = Predicates.ok();

	@NonNull
	private final Function<V, I> itemSelector;

	@NonNull
	private final Collection<V> collection;


	@Override
	public void write(T instance) {
		var items = getter.apply(instance);

		collection.forEach(v ->
			Optional.ofNullable(itemSelector.apply(v))
					.filter(predicate)
					.ifPresent(items::add)
		);
	}
}
