package org.dotspace.oofp.support.validation;

public class ValidationCompositions {

	public static <T> ValidationComposition<T> composing() {
		return new ValidationComposition<T>();
	}

}
