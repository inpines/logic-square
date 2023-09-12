package org.dotspace.creation.functional;

import java.util.function.Function;

public class Casters {

	public static <T> Function<Object, T> cast(Class<T> clazz) {
		return value -> {
			@SuppressWarnings("unchecked")
			T result = (T) value;
			return result;
		};
	}

}
