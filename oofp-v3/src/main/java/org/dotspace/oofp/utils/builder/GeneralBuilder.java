package org.dotspace.oofp.utils.builder;

import org.dotspace.oofp.utils.builder.operation.WriteOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class GeneralBuilder<P, T> {

	private final Function<P, T> constructor;
	private final List<WriteOperation<T>> writers = new ArrayList<>();
	
	public GeneralBuilder(Supplier<T> supplier) {
		super();
		this.constructor = x -> supplier.get();
	}

	public GeneralBuilder(Function<P, T> constructor) {
		super();
		this.constructor = constructor;
	}

	public T build() {
        return Optional.ofNullable(constructor.apply(null))
                .map(x -> {
                    writers.forEach(writer -> writer.write(x));
                    return x;
                })
                .orElse(null);
	}

	public T build(P arg) {
        return Optional.ofNullable(arg)
                .map(constructor)
                .map(x -> {
                    writers.forEach(writer -> writer.write(x));
                    return x;
                })
                .orElse(null);
	}
	
	public GeneralBuilder<P, T> with(WriteOperation<T> writer) {
		writers.add(writer);
		return this;
	}
	
	public GeneralBuilder<P, T> with(
			Collection<WriteOperation<T>> writer) {
		writers.addAll(writer);
		return this;
	}
	
}
