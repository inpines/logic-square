package org.dotspace.v1.creation.functional;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

public class Predicates {

	public static <T> Predicate<T> forPresent() {
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
