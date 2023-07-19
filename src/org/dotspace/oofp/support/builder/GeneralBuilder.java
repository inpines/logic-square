package org.dotspace.oofp.support.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class GeneralBuilder<P, T> {

	private Function<P, T> constructor;
	private List<GeneralBuildingWriter<T, ?>> writers = new ArrayList<>();
	
	public GeneralBuilder(Supplier<T> supplier) {
		super();
		this.constructor = x -> supplier.get();
	}

	public GeneralBuilder(Function<P, T> constructor) {
		super();
		this.constructor = constructor;
	}

	public T build() {
		T result = Optional.ofNullable(constructor.apply(null))
				.map(x -> {
					writers.forEach(writer -> writer.write(x));
					return x;
				})
				.orElse(null);
		return result;
	}

	public T build(P arg) {
		T result = Optional.ofNullable(arg)
				.map(constructor)
				.map(x -> {
					writers.forEach(writer -> writer.write(x));
					return x;
				})
				.orElse(null);
		return result;
	}
	
	public <V> GeneralBuilder<P, T> with(GeneralBuildingWriter<T, V> writer) {
		writers.add(writer);
		return this;
	}
	
	public <V, C> GeneralBuilder<P, T> with(
			Collection<GeneralBuildingWriter<T, V>> writer) {
		writers.addAll(writer);
		return this;
	}
	
}
