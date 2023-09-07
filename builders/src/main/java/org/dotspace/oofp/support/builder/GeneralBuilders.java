package org.dotspace.oofp.support.builder;

import java.util.function.Function;
import java.util.function.Supplier;

public class GeneralBuilders {

	public static <P, T> GeneralBuilder<P, T> of(Supplier<T> supplier) {
		return new GeneralBuilder<P, T>(supplier);
	}

	public static <P, T> GeneralBuilder<P, T> of(Function<P, T> applier) {
		return new GeneralBuilder<P, T>(applier);
	}
}
