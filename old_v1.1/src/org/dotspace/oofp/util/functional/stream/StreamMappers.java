package org.dotspace.oofp.util.functional.stream;

import java.util.function.Function;
import java.util.stream.Stream;

import org.dotspace.oofp.util.functional.StreamMapper;

public abstract class StreamMappers {

	private StreamMappers() {
		super();
	}
	
	public static <T, R> StreamMapper<T, R> mapSingular(Function<T, R> f) {
		return new SingularStreamMapper<>(f);
	}
	
	public static <T, R> StreamMapper<T, R> mapPlural(Function<T, Stream<R>> f) {
		return new PluralStreamMapper<>(f);
	}
	
}
