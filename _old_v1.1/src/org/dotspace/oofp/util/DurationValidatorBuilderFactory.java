package org.dotspace.oofp.util;

import java.util.Date;
import java.util.function.Function;

public interface DurationValidatorBuilderFactory {

	public <T> DurationValidatorBuilder<T> of(
			Function<T, Date> lowerDateReader, Function<T, Date> upperDateReader);
}
