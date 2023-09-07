package org.dotspace.oofp.util.functional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

public class Functions {

	private Functions() {
		super();
	}
	
	public static <T> Function<T, T> self() {
		return t -> t;
	}

	public static Function<String, Date> toDateTime() {
		return s -> Date.from(LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmSS"))
				.atZone(ZoneId.systemDefault()).toInstant());
	}
	
}
