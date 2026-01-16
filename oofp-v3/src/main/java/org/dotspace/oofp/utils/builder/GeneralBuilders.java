package org.dotspace.oofp.utils.builder;

import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class GeneralBuilders {

	public <P, T> GeneralBuilder<P, T> supply(Supplier<T> supplier) {
		return new GeneralBuilder<>(supplier);
	}

	public <P, T> GeneralBuilder<P, T> apply(Function<P, T> applier) {
		return new GeneralBuilder<>(applier);
	}

}
