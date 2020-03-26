package org.dotspace.creation.expression;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.dotspace.creation.AssignmentExpression;

public class Assignments {

	public static <T, V> AssignmentExpression<T, V> set(BiConsumer<T, V> setter, V value) {
		return RootAssignment.withAssignment(setter, value);
	}
	
	public static <T, M, V> AssignmentExpression<T, V> set(Function<T, M> getter,
			BiConsumer<M, V> setter, V value) {
		return SingularMemberAssignment.withAssignment(getter, setter, value);
	}
	
	public static <T, I, D> AssignmentExpression<T, D> setForEach(
			Function<T, Collection<I>> getter, Function<D, I> itemSelector, 
			Collection<D> datas) {
		return PluralMemberAssignment.withAssignment(getter, itemSelector, datas);
	}
	
}
