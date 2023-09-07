package org.dotspace.oofp.util.functional;

import java.util.stream.Stream;

public interface StreamMapper<T, R> {

	public Stream<R> apply(Stream<T> stream);
	
}
