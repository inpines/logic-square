package org.dotspace.oofp.utils.functional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@UtilityClass
public class Predicates {

	public <T> Predicate<T> ok() {
		
		return t -> true;
		
	}
	
	public <T> Predicate<T> isPresent() {
		return value -> {
			if (Objects.isNull(value)) {
				return false;
			}
			
			if (value instanceof String) {
				return Optional.of(value)
						.map(Casters.<String>cast())
						.map(s -> !StringUtils.isBlank(s))
						.orElse(false);
			}
			
			return true;
		};
	}

	public <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

	public <T> Function<Object, Predicate<T>> isNotPresent() {
		return x -> not(Predicates.isPresent());
	}
	
	public <T> Predicate<T> of(Predicate<T> predicate) {
		return predicate;
	}
	
	public <T> Predicate<T> any(final List<T> values) {
		
		return t ->  values.stream().anyMatch(t::equals);
		
	}
	
	public <T> Predicate<T> all(final List<T> values) {
		return t -> values.stream().allMatch(t::equals);
	}

	public <T> Predicate<T> present(Function<T, Object> reader) {
		return t -> Optional.of(t)
				.map(reader)
				.filter(x -> !StringUtils.isBlank(x.toString()))
				.isPresent();
	}

	public <T extends Comparable<T>> Predicate<T> between(T start, T end) {
		return x -> x.compareTo(start) >= 0 && x.compareTo(end) <= 0;
	}

}
