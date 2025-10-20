package org.dotspace.oofp.utils.functional;

import java.util.stream.Stream;

public interface StreamMapper<T, R> {

	Stream<R> apply(Stream<T> stream);
	
}
