package org.dotspace.oofp.support.validation;

public class ValidationCompositions {

	public static <T> ValidationBuilder<T> composing() {
		return new ValidationBuilder<T>();
	}

}
