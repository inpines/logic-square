package org.dotspace.oofp.support.orm.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParametersSpecificationTest {

    private CriteriaBuilder cb;
    private CriteriaQuery<?> query;
    private Root<Object> root;

    @BeforeEach
    void setUp() {
        cb = mock(CriteriaBuilder.class);
        query = mock(CriteriaQuery.class);
        root = mock(Root.class);
    }

    @Test
    void toPredicate_WithMultipleParameters() {
        Path<Object> pathA = mock(Path.class);
        Path<Object> pathB = mock(Path.class);
        when(root.get("a")).thenReturn(pathA);
        when(root.get("b")).thenReturn(pathB);

        Predicate pA = mock(Predicate.class);
        Predicate pB = mock(Predicate.class);
        when(cb.equal(pathA, 1)).thenReturn(pA);
        when(cb.equal(pathB, "x")).thenReturn(pB);

        Predicate combined = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        ParametersSpecification<Object> spec = new ParametersSpecification<>(Map.of("a", 1, "b", "x"));

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(combined, result);

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        Predicate[] passed = captor.getValue();
        assertEquals(2, passed.length);
        // ensure both predicates included
        assertTrue(java.util.Arrays.asList(passed).containsAll(java.util.List.of(pA, pB)));
    }

    @Test
    void toPredicate_IgnoresNullValues() {
        Path<Object> pathA = mock(Path.class);
        when(root.get("a")).thenReturn(pathA);

        Predicate pA = mock(Predicate.class);
        when(cb.equal(pathA, 1)).thenReturn(pA);

        Predicate combined = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        // use a mutable map that accepts null values instead of Map.of(...)
        Map<String, Object> params = new HashMap<>();
        params.put("a", 1);
        params.put("b", null);

        ParametersSpecification<Object> spec = new ParametersSpecification<>(params);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        Predicate[] passed = captor.getValue();
        assertEquals(1, passed.length);
        assertEquals(pA, passed[0]);

        // ensure cb.equal was never called for the null value 'b'
        verify(cb, never()).equal(any(), eq(null));
    }

    @Test
    void toPredicate_WithNullParametersTreatsAsEmpty() {
        Predicate combined = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        ParametersSpecification<Object> spec = new ParametersSpecification<>(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertSame(combined, result);

        ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
        verify(cb).and(captor.capture());
        assertEquals(0, captor.getValue().length);
    }
}
