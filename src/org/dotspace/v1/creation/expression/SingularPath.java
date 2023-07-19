package org.dotspace.v1.creation.expression;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.dotspace.v1.creation.AssignmentExpression;
import org.dotspace.v1.creation.AssignmentPath;

public class SingularPath<T, M, V> implements AssignmentPath<T, V> {

	private Function<T, M> getter;
	private BiConsumer<M, V> setter;
	
	public static <T, M, V> SingularPath<T, M, V> getPathToSet(Function<T, M> getter,
			BiConsumer<M, V> setter) {
		return new SingularPath<>(getter, setter);
	}
	
	public static <T, V> SingularPath<T, T, V> getRootToSet(BiConsumer<T, V> setter) {
		return new SingularPath<>(t -> t, setter);
	}
	
	private SingularPath(Function<T, M> getter, BiConsumer<M, V> setter) {
		super();
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public AssignmentExpression<T, V> assign(V value) {
		return SingularMemberAssignment.get(getter, setter, value);
	}

}
