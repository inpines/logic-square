package org.dotspace.oofp.support.orm.specification.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotspace.oofp.support.orm.specification.CriteriaOrder;
import org.dotspace.oofp.support.orm.specification.CriteriaPredicateExpression;
import org.dotspace.oofp.support.orm.specification.GeneralSpecification;
import org.dotspace.oofp.support.orm.specification.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SpecificationBuilderBase<T> implements SpecificationBuilder<T> {

	private final Map<String, CriteriaPredicateExpression<T>> expressions = new HashMap<>();
	
	private final List<CriteriaOrder> orders = new ArrayList<>();
	
	@Override
	public SpecificationBuilder<T> add(String name, CriteriaPredicateExpression<T> expression) {
		expressions.put(name, expression);
		return this;
	}

	@Override
	public SpecificationBuilder<T> orderBy(CriteriaOrder order) {
		this.orders.add(order);
		return this;
	}

	@Override
	public SpecificationBuilder<T> orderBy(List<CriteriaOrder> orders) {
		this.orders.addAll(orders);
		return this;
	}

	@Override
	public Specification<T> build() {
		return new GeneralSpecification<>(expressions, orders);
	}

	@Override
	public <C> SpecificationBuilder<T> add(String name, Supplier<CriteriaPredicateExpression<T>> expressionSupplier,
										   Predicate<C> predicate, C condition) {
		return Optional.ofNullable(condition)
				.filter(predicate)
				.map(c -> add(name, expressionSupplier.get()))
				.orElse(this);
	}

}
