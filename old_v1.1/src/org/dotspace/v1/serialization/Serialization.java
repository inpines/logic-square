package org.dotspace.v1.serialization;

import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;

public interface Serialization<T> {

	public Serialization<T> trace(Function<T, T> tracer);
	
	public <V> V transform(TypeReference<V> typeRef);
	
}
