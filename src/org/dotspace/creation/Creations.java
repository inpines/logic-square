package org.dotspace.creation;

import java.util.function.Supplier;

public class Creations {

	public static <T> CreationBuilder<T> construct(Supplier<T> constructor) {
		return new CreationBuilder<T>(constructor);
	}

}
