package org.dotspace.oofp.utils.functional;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

@UtilityClass
public class Functions {

	public <T> Function<T, T> self() {
		return t -> t;
	}

	public <T, C extends Collection<T>> Function<C, C> addAll(Collection<T> other) {
		return collection -> {
			collection.addAll(other);
			return collection;
		};
	}

	public Function<String, Date> toDateTime() {
		return s -> Date.from(LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmSS"))
				.atZone(ZoneId.systemDefault()).toInstant());
	}
	
}
