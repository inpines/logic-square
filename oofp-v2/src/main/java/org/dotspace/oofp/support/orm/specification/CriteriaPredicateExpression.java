package org.dotspace.oofp.support.orm.specification;

import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface CriteriaPredicateExpression<T> {

	Optional<Predicate> getPredicate(CriteriaBuilder cb, Root<T> root, String name);

}
