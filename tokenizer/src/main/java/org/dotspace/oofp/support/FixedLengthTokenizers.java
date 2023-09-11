package org.dotspace.oofp.support;

import java.util.function.Function;
import java.util.function.Supplier;

public interface FixedLengthTokenizers {

	public <T> FixedLengthTokenizer<?, T> tokenize(Supplier<T> supplier);

	public <A, T> FixedLengthTokenizer<A, T> tokenize(Function<A, T> constructor, A args);

}