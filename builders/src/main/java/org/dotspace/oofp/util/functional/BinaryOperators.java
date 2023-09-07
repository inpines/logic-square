package org.dotspace.oofp.util.functional;

import java.util.function.BinaryOperator;

public class BinaryOperators {

	private BinaryOperators() {
		super();
	}
	
	public static <T> BinaryOperator<T> first() {
		return (l, r) -> l;
	}
	
	public static <T> BinaryOperator<T> last() {
		return (l, r) -> r;
	}
	
	public static <T extends Comparable<T>> BinaryOperator<T> min() {
		return (l, r) -> {
			if (l.compareTo(r) >= 0) {
				return r;
			}
			
			return l;
		};
	}
	
	public static <T extends Comparable<T>> BinaryOperator<T> max() {
		return (l, r) -> {
			if (l.compareTo(r) > 0) {
				return l;
			}
			
			return r;
		};
	}
	
	public static BinaryOperator<Integer> totalInteger() {
		return (sum, i) -> {
			return sum + i;
		};
	}
	
	public static BinaryOperator<Long> totalLong() {
		return (sum, l) -> {
			return sum + l;
		};
	}
	
}
