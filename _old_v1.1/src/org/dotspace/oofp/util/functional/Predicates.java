package org.dotspace.oofp.util.functional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Predicates {

	private static final Log logger = LogFactory.getLog(Predicates.class);

	private Predicates() {
		super();
	}
	
	public static <T> Predicate<T> byPass(final Class<T> clazz) {
		
		return t -> {
				logger.debug("predidate by pass clazz of :".concat(clazz.getCanonicalName()));
				return true;			
		};
		
	}
	
	public static <T> Predicate<T> isPresent() {
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

	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

	public static <T> Function<Object, Predicate<T>> isNotPresent() {
		return x -> not(Predicates.isPresent());
	}
	
//	public static <T> Predicate<T> not(final Predicate<T> predicate) {
//		
//		return t -> !predicate.test(t);
//			
//	}

	public static <T> Predicate<T> of(Predicate<T> predicate) {
		return predicate;
	}
	
	public static <T> Predicate<T> any(final List<T> values) {
		
		return t ->  values.stream().anyMatch(t::equals);
		
	}
	
	public static <T> Predicate<T> all(final List<T> values) {
		return t -> values.stream().allMatch(t::equals);
	}

	public static <T> Predicate<T> present(Function<T, Object> reader) {
		return t -> Optional.of(t)
				.map(reader)
				.filter(x -> !StringUtils.isBlank(x.toString()))
				.isPresent();
	}
	
}
