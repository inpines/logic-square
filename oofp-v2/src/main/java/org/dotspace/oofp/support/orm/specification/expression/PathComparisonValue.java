package org.dotspace.oofp.support.orm.specification.expression;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.dotspace.oofp.support.orm.specification.CriteriaPredicateExpression;

public class PathComparisonValue<T, V> implements CriteriaPredicateExpression<T> {

	private final Function<CriteriaBuilder, BiFunction<Path<V>, V, Predicate>> comparisonFactory;

	protected V value;
	
	public PathComparisonValue(
			Function<CriteriaBuilder, BiFunction<Path<V>, V, Predicate>> comparisonFactory,
			V value) {
		super();
		this.comparisonFactory = comparisonFactory;
		this.value = value;
	}

	@Override
	public Optional<Predicate> getPredicate(CriteriaBuilder cb, Root<T> root, String name) {
		Path<V> path = root.get(name);

		BiFunction<Path<V>, V, Predicate> comparison = Optional.of(cb)
				.map(comparisonFactory)
				.orElse(null);
		
		if (null == comparison) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(value)
				.map(val -> comparison.apply(path, value));
	}

}
