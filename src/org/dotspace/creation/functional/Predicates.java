package org.dotspace.creation.functional;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.platform.commons.util.StringUtils;

public class Predicates {

	public static <T> Predicate<T> present() {
		return value -> {
			if (Objects.isNull(value)) {
				return false;
			}
			
			if (value instanceof String) {
				return Optional.ofNullable(value)
						.map(Casters.cast(String.class))
						.map(s -> !StringUtils.isBlank(s))
						.orElse(false);
			}
			
			return true;
		};
	}

}
