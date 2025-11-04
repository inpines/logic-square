package org.dotspace.oofp.support.orm.specification;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {

	SpecificationBuilder<T> add(String name, CriteriaPredicateExpression<T> expression);
	
	<C> SpecificationBuilder<T> add(String name, Supplier<CriteriaPredicateExpression<T>> expressionSupplier,
			Predicate<C> predicate, C condition);
	
	SpecificationBuilder<T> orderBy(CriteriaOrder order);
	
	SpecificationBuilder<T> orderBy(List<CriteriaOrder> orders);
	
	Specification<T> build();
	
}
