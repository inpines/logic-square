package org.dotspace.oofp.support.validation;

public class GenenalValidations {

	public static <T> ValidationBuilder<T> compose() {
		return new ValidationBuilder<T>();
	}

}
