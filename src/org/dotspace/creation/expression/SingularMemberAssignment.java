package org.dotspace.creation.expression;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SingularMemberAssignment<T, M, V> extends AssignmentExpressionBase<T, V> {

	private Function<T, M> getter;
	private BiConsumer<M, V> setter;
	private V value;
		
	private SingularMemberAssignment(Function<T, M> getter, 
			BiConsumer<M, V> setter, V value) {
		this.getter = getter;
		this.setter = setter;
		this.value = value;
	}
	
	@Override
	public void assign(T instance) {
		if (!isConditionPresent(instance) || null == setter) {
			return;
		}
		
		M member = Optional.ofNullable(getter)
				.map(reader -> reader.apply(instance))
				.orElse(Optional.ofNullable(instance)
						.map(inst -> cast(inst))
						.orElse(null));
		
		setter.accept(member, value);
	}

	private M cast(T inst) {
		@SuppressWarnings("unchecked")
		M result = (M) inst;
		return result;
	}

	public static <T, M, V> SingularMemberAssignment<T, M, V> withAssignment(
			Function<T, M> getter, BiConsumer<M, V> setter, V value) {
		return new SingularMemberAssignment<>(getter, setter, value);
	}

}
