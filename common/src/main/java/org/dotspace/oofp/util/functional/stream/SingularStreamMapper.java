package org.dotspace.oofp.util.functional.stream;

import java.util.function.Function;
import java.util.stream.Stream;

import org.dotspace.oofp.util.functional.StreamMapper;

public class SingularStreamMapper<T, R> implements StreamMapper<T, R> {

	private Function<T, R> f;
	
	protected SingularStreamMapper(Function<T, R> f) {
		this.f = f;
	}
	
	@Override
	public Stream<R> apply(Stream<T> stream) {
		return stream.map(f);
	}

}
