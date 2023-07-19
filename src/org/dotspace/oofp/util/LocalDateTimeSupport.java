package org.dotspace.oofp.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public abstract class LocalDateTimeSupport {
	
	public static Optional<Date> getDatetime(LocalDateTime ldt) {
		return Optional.ofNullable(ldt.atZone(ZoneId.systemDefault()))
				.map(zDt -> zDt.toInstant())
				.map(instnt -> Date.from(instnt));
	}
	
}
