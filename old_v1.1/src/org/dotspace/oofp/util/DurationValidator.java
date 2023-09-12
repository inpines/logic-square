package org.dotspace.oofp.util;

public interface DurationValidator<T> {
	
	boolean validate(T source);
	
	boolean validateOuter(T source);

	boolean validateInner(T source);

	boolean validateLowerBound(T source);

	boolean validateUpperBound(T source);

}