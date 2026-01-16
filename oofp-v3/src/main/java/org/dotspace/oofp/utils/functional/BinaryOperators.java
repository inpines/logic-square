package org.dotspace.oofp.utils.functional;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.function.BinaryOperator;

@UtilityClass
public class BinaryOperators {

	public <T> BinaryOperator<T> first() {
		return (l, r) -> l;
	}
	
	public <T> BinaryOperator<T> last() {
		return (l, r) -> r;
	}

	public <T> BinaryOperator<Collection<T>> addAll() {
		return (c1, c2) -> {
			c1.addAll(c2);
			return c1;
		};
	}

	public <T extends Comparable<T>> BinaryOperator<T> min() {
		return (l, r) -> {
			if (l.compareTo(r) >= 0) {
				return r;
			}
			
			return l;
		};
	}
	
	public <T extends Comparable<T>> BinaryOperator<T> max() {
		return (l, r) -> {
			if (l.compareTo(r) > 0) {
				return l;
			}
			
			return r;
		};
	}
	
	public BinaryOperator<Integer> totalInteger() {
		return Integer::sum;
	}
	
	public BinaryOperator<Long> totalLong() {
		return Long::sum;
	}
	
}
