package org.dotspace.oofp.support.orm.specification;

import java.util.Map;

import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class QuerySpecifications {

	public static <T> Specification<T> findByParameters(Map<String, Object> criteria) {
		return new ParametersSpecification<>(criteria);
	}
	
	public static <T> Specification<T> findByPredicateExpressions(
			SpecificationBuilder<T> specificationBuilder) {
		return specificationBuilder.build();
	}
	
}
