package org.dotspace.oofp.support.orm.specification.expression;

import java.util.function.BiFunction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CriteriaBuilderComparisons {

	public static <V> BiFunction<Path<V>, V, Predicate> pathValueEq(CriteriaBuilder cb) {
		return cb::equal;
	}

	public static <V> BiFunction<Path<V>, V, Predicate> pathValueNotEq(CriteriaBuilder cb) {
		return cb::notEqual;
	}

	public static <V extends Comparable<V>> BiFunction<Path<V>, V, Predicate> pathValueGt(
			CriteriaBuilder cb) {
		return cb::greaterThan;
	}
	
	public static <V extends Comparable<V>> BiFunction<Path<V>, V, Predicate> pathValueGe(
			CriteriaBuilder cb) {
		return cb::greaterThanOrEqualTo;
	}
	
	public static <V extends Comparable<V>> BiFunction<Path<V>, V, Predicate> pathValueLt(
			CriteriaBuilder cb) {
		return cb::lessThan;
	}
	
	public static <V extends Comparable<V>> BiFunction<Path<V>, V, Predicate> pathValueLe(
			CriteriaBuilder cb) {
		return cb::lessThanOrEqualTo;
	}
	
	public static BiFunction<Path<String>, String, Predicate> pathValueLike(
			CriteriaBuilder cb) {
		return cb::like;
	}
}
