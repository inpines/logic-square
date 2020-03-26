package org.dotspace.creation.expression;

import java.util.Collection;
import java.util.function.Function;

import org.dotspace.creation.AssignmentExpression;
import org.dotspace.creation.AssignmentPath;

public class PluralMemberPath<T, I, D> implements AssignmentPath<T, Collection<D>> {

	private Function<T, Collection<I>> getter;
	
	private Function<D, I> itemSelector;

	public static <T, I, D> PluralMemberPath<T, I, D> getToSet(
			Function<T, Collection<I>> getter, Function<D, I> itemSelector) {
		return new PluralMemberPath<>(getter, itemSelector);
	}
	
	private PluralMemberPath(Function<T, Collection<I>> getter, 
			Function<D, I> itemSelector) {
		super();
		this.getter = getter;
		this.itemSelector = itemSelector;
	}

	@Override
	public AssignmentExpression<T, Collection<D>> assign(Collection<D> data) {
		return PluralMemberAssignment.get(getter, itemSelector, data);
	}

}
