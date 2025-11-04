package org.dotspace.oofp.support.orm.specification.expression;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked"})
class CriteriaBuilderComparisonsTest {

    @Test
    void pathValueEq_invokesEqual() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<String> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.equal(path, "value")).thenReturn(expected);

        BiFunction<Path<String>, String, Predicate> fn = CriteriaBuilderComparisons.pathValueEq(cb);
        Predicate actual = fn.apply(path, "value");

        assertSame(expected, actual);
        verify(cb).equal(path, "value");
    }

    @Test
    void pathValueNotEq_invokesNotEqual() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<String> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.notEqual(path, "x")).thenReturn(expected);

        BiFunction<Path<String>, String, Predicate> fn = CriteriaBuilderComparisons.pathValueNotEq(cb);
        Predicate actual = fn.apply(path, "x");

        assertSame(expected, actual);
        verify(cb).notEqual(path, "x");
    }

    @Test
    void pathValueGt_invokesGreaterThan() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Integer> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.greaterThan(path, 10)).thenReturn(expected);

        BiFunction<Path<Integer>, Integer, Predicate> fn = CriteriaBuilderComparisons.pathValueGt(cb);
        Predicate actual = fn.apply(path, 10);

        assertSame(expected, actual);
        verify(cb).greaterThan(path, 10);
    }

    @Test
    void pathValueGe_invokesGreaterThanOrEqualTo() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Integer> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.greaterThanOrEqualTo(path, 5)).thenReturn(expected);

        BiFunction<Path<Integer>, Integer, Predicate> fn = CriteriaBuilderComparisons.pathValueGe(cb);
        Predicate actual = fn.apply(path, 5);

        assertSame(expected, actual);
        verify(cb).greaterThanOrEqualTo(path, 5);
    }

    @Test
    void pathValueLt_invokesLessThan() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Integer> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.lessThan(path, 7)).thenReturn(expected);

        BiFunction<Path<Integer>, Integer, Predicate> fn = CriteriaBuilderComparisons.pathValueLt(cb);
        Predicate actual = fn.apply(path, 7);

        assertSame(expected, actual);
        verify(cb).lessThan(path, 7);
    }

    @Test
    void pathValueLe_invokesLessThanOrEqualTo() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Integer> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.lessThanOrEqualTo(path, 3)).thenReturn(expected);

        BiFunction<Path<Integer>, Integer, Predicate> fn = CriteriaBuilderComparisons.pathValueLe(cb);
        Predicate actual = fn.apply(path, 3);

        assertSame(expected, actual);
        verify(cb).lessThanOrEqualTo(path, 3);
    }

    @Test
    void pathValueLike_invokesLike() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<String> path = mock(Path.class);
        Predicate expected = mock(Predicate.class);

        when(cb.like(path, "%pat%")).thenReturn(expected);

        BiFunction<Path<String>, String, Predicate> fn = CriteriaBuilderComparisons.pathValueLike(cb);
        Predicate actual = fn.apply(path, "%pat%");

        assertSame(expected, actual);
        verify(cb).like(path, "%pat%");
    }
}
