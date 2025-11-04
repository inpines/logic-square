package org.dotspace.oofp.support.orm.specification;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.lang.Nullable;

@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ParametersSpecification<T> implements Specification<T> {

	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 3968961473115545162L;
	
	private transient Map<String, Object> parameters;

	@Override
	public Predicate toPredicate(@NonNull Root<T> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder cb) {
		List<Predicate> criteria = Maybe.given(parameters).orElse(Map.of())
				.entrySet().stream()
				.filter(e -> null != e.getValue())
				.map(e -> cb.equal(root.get(e.getKey()), e.getValue()))
				.toList();
		return cb.and(criteria.toArray(new Predicate[0]));
	}

}
