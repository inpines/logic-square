package org.dotspace.oofp.util.functional;

import java.util.function.Predicate;

public class Predication<T> {

	private Predicate<T> predicate;
	private T operant;
	
	public static <T> Predication<T> of(Predicate<T> predicate, T operant) {
		return new Predication<>(predicate, operant);
	}
	
	private Predication(Predicate<T> predicate, T operant) {
		this.predicate = predicate;
		this.operant = operant;
	}

	public boolean test() {
		if (null == predicate) {
			return true;
		}
		
		return predicate.test(operant);
	}
}
