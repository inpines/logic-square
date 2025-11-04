package org.dotspace.oofp.support.orm.specification.expression;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PathValueGtTest {

    @Test
    void testGetPredicateWithMocks() {
        Integer value = 10;
        PathValueGt<Object, Integer> gt = new PathValueGt<>(value);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Object> root = mock(Root.class);
        Path<Integer> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("testField")).thenReturn((Path) path);
        when(cb.greaterThan(path, value)).thenReturn(predicate);

        Optional<Predicate> result = gt.getPredicate(cb, root, "testField");

        assertTrue(result.isPresent());
        assertEquals(predicate, result.get());
        verify(root).get("testField");
        verify(cb).greaterThan(path, value);
    }
}