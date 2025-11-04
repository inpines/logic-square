package org.dotspace.oofp.support.orm.specification;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.lang.Nullable;

@AllArgsConstructor
public class GeneralSpecification<T> implements Specification<T> {

	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = -3166427510755038207L;
	
	private transient Map<String, CriteriaPredicateExpression<T>> predicateExpressions;
	private transient List<CriteriaOrder> orders;
	
	@Override
	public Predicate toPredicate(
			@NonNull Root<T> root, @Nullable CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
		List<Predicate> criteria = predicateExpressions.entrySet().stream()
				.map(e -> getPredicate(
						cb, root, e.getKey(), e.getValue()))
				.flatMap(Optional::stream)
				.toList();
		Maybe.given(query)
				.match(q -> q.orderBy(orders.stream()
						.map(ord -> getOrder(cb, root, ord))
						.flatMap(Optional::stream)
						.toList())
				);
		return cb.and(criteria.toArray(new Predicate[0]));
	}
	
	private Optional<Predicate> getPredicate(CriteriaBuilder cb, Root<T> root, String name, 
			CriteriaPredicateExpression<T> comparison) {
		return comparison.getPredicate(cb, root, name);
	}

	private Optional<Order> getOrder(
			CriteriaBuilder cb, Root<T> root, CriteriaOrder order) {
		if ("ascending".equalsIgnoreCase(order.getType())) {
			return Optional.ofNullable(cb.asc(root.get(order.getName())));
		}

		if ("descending".equalsIgnoreCase(order.getType())) {
			return Optional.ofNullable(cb.desc(root.get(order.getName())));
		}

		return Optional.empty();
	}
}
