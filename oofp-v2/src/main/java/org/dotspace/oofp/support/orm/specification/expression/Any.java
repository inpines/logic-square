package org.dotspace.oofp.support.orm.specification.expression;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.dotspace.oofp.support.orm.specification.CriteriaPredicateExpression;
import org.dotspace.oofp.utils.functional.Predicates;

public class Any<T> implements CriteriaPredicateExpression<T> {

	private final List<CriteriaPredicateExpression<T>> predicateExpressions;

	public Any(List<CriteriaPredicateExpression<T>> predicateExpressions) {
		super();
		this.predicateExpressions = predicateExpressions;
	}

	@Override
	public Optional<Predicate> getPredicate(CriteriaBuilder cb, Root<T> root, String name) {
		List<Predicate> predicates = Optional.ofNullable(predicateExpressions)
				.filter(Predicates.not(List::isEmpty))
				.map(pes -> pes.stream()
						.map(exp -> exp.getPredicate(cb, root, name))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.toList())
				.orElse(Collections.emptyList());
		
		return predicates.isEmpty() ? Optional.empty()
				: Optional.of(cb.or(predicates.toArray(new Predicate[0])));
	}

}
