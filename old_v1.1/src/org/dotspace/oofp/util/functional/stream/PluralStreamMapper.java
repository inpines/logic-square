package org.dotspace.oofp.util.functional.stream;

import java.util.function.Function;
import java.util.stream.Stream;

import org.dotspace.oofp.util.functional.StreamMapper;

public class PluralStreamMapper<T, R> implements StreamMapper<T, R> {

	private Function<T, Stream<R>> f;

	protected PluralStreamMapper(Function<T, Stream<R>> f) {
		super();
		this.f = f;
	}

	@Override
	public Stream<R> apply(Stream<T> stream) {
		return stream.flatMap(f);
	}

}
