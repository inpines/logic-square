package org.dotspace.oofp.util;

import java.util.Date;

public interface DurationValidatorBuilder<T> {

	public DurationValidatorBuilder<T> from(Date date);
	
	public DurationValidatorBuilder<T> to(Date date);
	
	public DurationValidator<T> getValidator();
	
}
