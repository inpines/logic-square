package org.dotspace.oofp.support.orm.specification.expression;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.dotspace.oofp.support.orm.specification.CriteriaPredicateExpression;

public class PathRangeBetween<T, V extends Comparable<V>> implements CriteriaPredicateExpression<T> {

	private final Pair<V, V> range;
		
	public PathRangeBetween(V low, V high) {
		super();
		
		this.range = Optional.ofNullable(low)
				.map(l -> Pair.of(l, high))
				.orElse(getPairNotSpecifiedLow(high));
	}

	private Pair<V, V> getPairNotSpecifiedLow(V high) {
		return Optional.ofNullable(high)
				.map(h -> Pair.of(h, h))
				.orElse(null);
	}

	@Override
	public Optional<Predicate> getPredicate(CriteriaBuilder cb, Root<T> root, String name) {
		return Optional.ofNullable(range)
				.map(rng -> {
					
					V l = rng.getLeft();
					V h = rng.getRight();
					
					Path<V> path = root.get(name);
					
					if (null == l) {
						return cb.lessThanOrEqualTo(path, h);
					}
					
					if (null == h) {
						return cb.greaterThan(path, l);
					}
					
					return cb.between(path, l, h);
				});
	}

}
