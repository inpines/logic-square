package org.dotspace.validation;

public class ModelValidations {

	public static <T> ValidationBuilder<T> typeOf(Class<T> clazz) {
		return new ValidationBuilder<T>(clazz);
	}

}
