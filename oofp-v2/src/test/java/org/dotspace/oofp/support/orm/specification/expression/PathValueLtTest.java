package org.dotspace.oofp.support.orm.specification.expression;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PathValueLtTest {

    @Test
    void testGetPredicateWithMocks() {
        Integer value = 5;
        PathValueLt<Object, Integer> lt = new PathValueLt<>(value);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Object> root = mock(Root.class);
        Path<Integer> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("testField")).thenReturn((Path) path);
        when(cb.lessThan(path, value)).thenReturn(predicate);

        Optional<Predicate> result = lt.getPredicate(cb, root, "testField");

        assertTrue(result.isPresent());
        assertEquals(predicate, result.get());
        verify(root).get("testField");
        verify(cb).lessThan(path, value);
    }
}