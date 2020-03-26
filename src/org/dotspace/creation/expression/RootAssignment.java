package org.dotspace.creation.expression;

import java.util.function.BiConsumer;

public class RootAssignment<T, V, C> extends AssignmentExpressionBase<T, V> {

	private BiConsumer<T, V> setter;

	private V value;
	
	public static <T, V, C> RootAssignment<T, V, C> withAssignment(
			BiConsumer<T, V> setter, V value) {
		return new RootAssignment<>(setter, value);
	}
	
	private RootAssignment(BiConsumer<T, V> setter, V value) {
		super();
		this.setter = setter;
		this.value = value;
	}

	@Override
	public void assign(T instance) {
		
		if (!isConditionPresent(instance) || null == setter) {
			return;
		}
		
		setter.accept(instance, value);
	}

}
